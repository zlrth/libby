(ns user
  (:require [mount.core :as mount]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [clojure.java.jdbc :as j]
            ;; [mysql-connector-java]
            libby.core))

(defn start []
  (mount/start-without #'libby.core/http-server
                       #'libby.core/repl-server))

(defn stop []
  (mount/stop-except #'libby.core/http-server
                     #'libby.core/repl-server))

(defn restart []
  (stop)
  (start))




(defn url->req
  [url]
  (let [req (client/get url {:as :byte-array :throw-exceptions false})]
    (if (= (:status req) 200)
      req)))

(defn req->file-name
  [req]
  (let [headers (:headers req)
        stringified (apply str (apply concat headers)) ;; TODO fix nested applys
        f (re-find #"(?<=filename..).*(?=\")" stringified)]
    f))

(defn save-binary!
  [url]
  (let [req (url->req url)]
    (with-open [w (io/output-stream (str "pdfs/" (req->file-name req)))]
      (.write w (:body req)))
    req))

(defn get-results [query]
  (let [search-body (:body (client/get (str "http://libgen.io/search.php?req=" query)))
        ads (re-seq #"ads.php[^']+" search-body)
        download-bodies (map #(:body (client/get (str "http://libgen.io/" %))) ads)
;;         ad (first ads)
        ;; page-body (:body (client/get (str "http://libgen.io/" ad)))
        direct-links (map  #(str "http://libgen.io/" (re-find #"get\.php[^']+"  %)) download-bodies)]
    direct-links))

(defn query->download-link [query]
  (let [search-body (:body (client/get (str "http://libgen.io/search.php?req=" query)))
        md5s (distinct (re-seq #"(?<=md5=).{32}" search-body))
        download-links (map (str "http://libgen.io/get/ %" md5s))
        ]))

(defn get-result [query]
  (let [search-body (:body (client/get (str "http://libgen.io/search.php?req=" query)))
        ads (re-seq #"ads.php[^']+" search-body)
        ad (first ads)
        download-body (:body (client/get (str "http://libgen.io/" ad)))
        direct-link (str "http://libgen.io/" (re-find #"get\.php[^']+"  download-body))]
    direct-link))


(def mysql-db {:dbtype "mysql"
               :dbname "bookwarrior"
               :user "root"
               :password ""
               })

(defn- map->download-link [{:keys [md5 author title extension] :as m}]
  (str "http://libgen.io/get/" md5 "/" author "-" title "." extension))

(defn- remove-empties [m] ;; http://stackoverflow.com/a/3938151
  (into {} (remove (comp #(= "" %) second) m)))

(defn md5->list-of-maps [md5]
  (first (j/query mysql-db ["select title,Extension,Filesize from updated where md5 = ?" md5])))


(defn map->map-with-download-link [m]
  (assoc m :download-link (map->download-link m)))

(defn big-map->download-links [big-map]
  (map map->download-link big-map))

(defn search->big-map [search]
  (let [big-map-with-empties (j/query mysql-db ["select * from updated where title like ?" (str search "%")])
        big-map (map remove-empties big-map-with-empties)
        big-map-with-download-links (map map->map-with-download-link big-map)]
    big-map-with-download-links))

