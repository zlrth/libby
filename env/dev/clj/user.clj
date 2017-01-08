(ns user
  (:require [mount.core :as mount]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [clj-http.client :as client]
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

(defn fetch-binary
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
  (let [req (fetch-binary url)]
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

(defn get-result [query]
  (let [search-body (:body (client/get (str "http://libgen.io/search.php?req=" query)))
        ads (re-seq #"ads.php[^']+" search-body)
        ad (first ads)
        download-body (:body (client/get (str "http://libgen.io/" ad)))
        direct-link (str "http://libgen.io/" (re-find #"get\.php[^']+"  download-body))]
    direct-link))
