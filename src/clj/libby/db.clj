(ns libby.db
  (:require [clojure.java.jdbc :as j]))

(def mysql-db {:dbtype "mysql"
               :dbname "bookwarrior"
               :user "root"
               :password ""
               })

(defn- map->download-link [{:keys [md5 author title extension] :as m}]
  (str "http://libgen.io/get/" md5 "/" author "-" title "." extension))

(defn- remove-empties [m] ;; http://stackoverflow.com/a/3938151
  (into {} (remove (comp #(= "" %) second) m)))

(defn map->map-with-download-link [m]
  (assoc m :download-link (map->download-link m)))

(defn fix-coverurls [big-map]
  (into () (map #(if (clojure.string/includes? (:coverurl %) "http" ) % (assoc % :coverurl (str "http://libgen.io/covers/" (:coverurl %)))) big-map)))

;; do a sql query, then clean up the sql query
(defn search->big-map [search]
  (let [big-map-with-empties (j/query mysql-db ["select * from updated where title like ?" (str "%" search "%")])
        big-map-without-download-links (map remove-empties big-map-with-empties)
        big-map (map map->map-with-download-link big-map-without-download-links)]
    big-map))

(defn search->single-download-link [search]
  (let [big-map (search->big-map search)
        download-link (:download-link (first big-map))]
    download-link))
