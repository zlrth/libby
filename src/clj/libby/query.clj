(ns libby.query
  (:require [libby.solr :as solr]))

(defn- remove-an-empty [m] ;; http://stackoverflow.com/a/3938151
  (into {} (remove (comp #(= "" %) second) m)))

(defn remove-empties [big-map]
  (map remove-an-empty big-map))

(defn devectorize [big-map]
  (map #(zipmap (keys %) (flatten (vals %))) big-map))

(defn fix-coverurls [big-map]
  (into () (map #(if ((fnil clojure.string/includes? "unfortunately-no-cover-is-found") (:coverurl %) "http" ) % (assoc % :coverurl (str "http://libgen.io/covers/" (:coverurl %)))) big-map)))

(defn download-link [{:keys [md5 author title extension :as m]}]
  (str "http://libgen.io/get/" md5 "/" author "-" title "." extension))

(defn assoc-download-links [big-map]
  (into () (map #(assoc % :download-link (download-link %)) big-map)))

(defn file-size-in-kbs [big-map]
  (into () (map #(assoc % :filesize (quot (read-string (:filesize %)) 1000)) big-map)))

(defn search->big-map [search]
  (-> search
      (solr/q {:rows 1000 :qf "author title"})
      :response
      :docs
      devectorize
      remove-empties
      fix-coverurls
      file-size-in-kbs
      assoc-download-links))

