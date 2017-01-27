(ns libby.routes.home
  (:require [libby.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [libby.db :as db]
            [libby.solr :as solr]))



(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))


(defn search->big-map [search]
  (do
    (solr/do-shit)
    (:docs (:response (solr/query @solr/system search {:edismax 1 :rows 100})))))

(defn results [query]
  (let [_ (log/info query)]
    (layout/render "results.html" {:query query :barf (search->big-map query)})))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" [] (home-page))
  (POST "/results" [query] (results query))
  (GET "/about" [] (about-page)))

