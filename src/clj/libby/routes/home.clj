(ns libby.routes.home
  (:require [libby.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clj-http.client :as client]
            [clojure.java.io :as io]))

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page []
  (layout/render "about.html"))

(defn get-result [query]
  (let [search-body (:body (client/get (str "http://libgen.io/search.php?req=" query)))
        ads (re-seq #"ads.php[^']+" search-body)
        ad (first ads)
        page-body (:body (client/get (str "http://libgen.io/" ad)))
        download-url (str "http://libgen.io/" (re-find #"get\.php[^']+"  page-body))
        ]
    download-url
    ))

(defn lucky []
    (layout/render "lucky.html"))

(defn lucky-result [query]
  (layout/render "lucky-result.html" {:link (get-result query)}))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/lucky" [] (lucky))
  (POST "/lucky-result" [query] (lucky-result query))
  (GET "/about" [] (about-page)))

