(ns user
  (:require [mount.core :as mount]
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


(defn fetch-binary!
  [url]
  (let [req (client/get url {:as :byte-array :throw-exceptions false})]
    (if (= (:status req) 200)
      req)))

(defn save-binary!
  [url]
  (let [req (fetch-binary! url)]
    (with-open [w (io/output-stream (str "pdfs/" "havingfun" ".pdf"))]
      (.write w (:body req)))
    req))

(defn get-results [query]
  (let [search-body (:body (client/get (str "http://libgen.io/search.php?req=" query)))
        ads (re-seq #"ads.php[^']+" search-body)
        page-bodies (map #(:body (client/get (str "http://libgen.io/" %))) ads)
;;         ad (first ads)
        ;; page-body (:body (client/get (str "http://libgen.io/" ad)))
        download-urls (map  #(str "http://libgen.io/" (re-find #"get\.php[^']+"  %)) page-bodies)
        ]
    download-urls
    ))

(defn get-result [query]
  (let [search-body (:body (client/get (str "http://libgen.io/search.php?req=" query)))
        ads (re-seq #"ads.php[^']+" search-body)
        ad (first ads)
        page-body (:body (client/get (str "http://libgen.io/" ad)))
        download-url (str "http://libgen.io/" (re-find #"get\.php[^']+"  page-body))
        ]
    download-url
    ))
