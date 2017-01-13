(ns user
  (:require [mount.core :as mount]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [clojure.java.jdbc :as j]
            ;; [mysql-connector-java]
            [libby.db :refer :all]
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

