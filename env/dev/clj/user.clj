(ns user
  (:require [mount.core :as mount]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [clojure.java.jdbc :as j]
            ;; [mysql-connector-java]
            [libby.db :refer :all]
            [libby.solr :as solr]
            [libby.query :as q]
            [flux.embedded :as e]
            [flux.core :as f]
            [clojure.tools.logging :refer :all]
            libby.core))

;; consider disabling font-lock-mode for faster repl printing
(defn setup []
  (let [container (e/create-core-container "resources/solr" "resources/solr/solr.xml")
        core (e/create container :libbyname)]
    core))

(def keys-in-solr [:id :author :lcc :md5 :publisher :series :ddc :identifierwodash :doi :title :asin :pages :identifier :filesize :openlibraryid :edition :coverurl])

(def core (setup))

;; (def fixture-data (map #(select-keys % keys-in-solr) (select-everything)))


(defn add-seq-of-docs [conn m]
  (f/with-connection conn (map #(f/add %) m) (f/commit)))

(defn mysql->solr [conn]
  (f/with-connection conn (j/query mysql-db
                                   ["select id,author,lcc,md5,publisher,series,ddc,identifierwodash,doi, title,asin,pages,filesize,openlibraryid,edition,coverurl from updated"]
                                   {:row-fn f/add
                                    }
                                   )
    (f/commit)))

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

