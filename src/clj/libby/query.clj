(ns libby.query
  (:require [flux.http :as http]
            [flux.cloud :as cloud]
            [flux.core :as flux]
            [libby.solr :as solr]))


(defn- remove-an-empty [m] ;; http://stackoverflow.com/a/3938151
  (into {} (remove (comp #(= "" %) second) m)))

(defn remove-empties [big-map]
  (map remove-an-empty big-map))

(defn devectorize [big-map]
  (map #(zipmap (keys %) (flatten (vals %))) big-map))

(defn fix-coverurls [big-map]
  (into () (map #(if ((fnil clojure.string/includes? "unfortunately-no-cover-is-found") (:coverurl_t %) "http" ) % (assoc % :coverurl_t (str "http://libgen.io/covers/" (:coverurl_t %)))) big-map)))

(defn download-link [{:keys [md5_t author_t title_t extension_t :as m]}]
  (str "http://libgen.io/get/" md5_t "/" author_t "-" title_t "." extension_t))

(defn assoc-download-links [big-map]
  (into () (map #(assoc % :download-link (download-link %)) big-map)))

(defn search->big-map [search]
  (solr/go)
  (-> search
      (solr/q {:edismax 1 :rows 1000})
      :response
      :docs
      devectorize
      remove-empties
      fix-coverurls
      assoc-download-links))

