(ns libby.routes.home
  (:require [libby.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.xml :as xml]
            [clojure.zip :as z]
            [clojure.string :as string]
            [cheshire.core :refer :all]
            [libby.db :as db]))


;; get isbn from http request at open library

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn results [query]
  (layout/render "results.html" {:barf       (db/search->big-map query)
                                :oplib       (client/get "http://openlibrary.org/search.json?q=clojure+joy")}))

(defn about-page []
  (layout/render "about.html"))

(defn lucky []
    (layout/render "lucky.html"))

(defn lucky-result [query]
  (layout/render "lucky-result.html" {:link        (db/search->single-download-link query)
                                      :oplib       (client/get "http://openlibrary.org/search.json?q=clojure+joy")}))

(defroutes home-routes
  (GET "/" [] (home-page))
  (POST "/results" [query] (results query))
  (GET "/lucky" [] (lucky))
  (POST "/lucky-result" [query] (lucky-result query))
  (GET "/about" [] (about-page)))

