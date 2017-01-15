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

(defn get-isbn-json [query]
  "helper function for get-isbn"
  (first (get (last (parse-string (:body (client/get (str "http://openlibrary.org/search.json?q=" query))))) 1)))

(defn get-isbn [query]
  (first (first (remove nil? (map #(if (= (first %) "isbn") (second %)) (get-isbn-json query))))))


(get-isbn "The Joy of Clojure")

(def get-isbn-memo (memoize get-isbn))

(get-isbn-memo "the joy of clojure")
(first (db/search->big-map "clojure"))
(take 2 (db/search->big-map "clojure"))


(defn add-isbn [query]
  (assoc (first (db/search->big-map query)) :isbn (get-isbn-memo query)))

(add-isbn "joy of clojure")

(pmap #(add-isbn (:title %)) (db/search->big-map "joy of clojure"))



(defn add-isbn-to-big-map [query]
  (map #(add-isbn (:title %)) (db/search->big-map query)))

(add-isbn-to-big-map "clojure")
(defn add-isbn-to-some-in-big-map [query n]
  (map #(add-isbn (:title %)) (take n (db/search->big-map query))))

;; take isbn from open library and get library of congress number from oclc
(comment
(defn get-llc-xml [isbn]
  "helper function for get-isbn"
  (:body (client/get (str "http://classify.oclc.org/classify2/Classify?isbn=" isbn "&summary=true"))))

(get-llc-xml "9783540634805")

(use 'clojure.data.xml)
(last (:content (clojure.data.xml/parse-str (get-llc-xml "9783540634805"))))

)

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

