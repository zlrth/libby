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

(defn search->big-map [search]
  (let [big-map-with-empties (j/query mysql-db ["select * from updated where title like ?" (str "%" search "%")])
        big-map (map remove-empties big-map-with-empties)
        big-map-with-download-links (map map->map-with-download-link big-map)]
    big-map-with-download-links))

(defn search->single-download-link [search]
  (let [big-map (search->big-map search)
        download-link (:download-link (first big-map))]
    download-link))

