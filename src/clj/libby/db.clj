(ns libby.db
  (:require [clojure.java.jdbc :as j]))

(def mysql-db {:dbtype "mysql"
               :dbname "bookwarrior"
               :user "root"
               :password ""})

(defn do-sql [search]
  (j/query mysql-db ["select * from updated where title like ?" (str "%" search "%")]))


(defn- remove-an-empty [m] ;; http://stackoverflow.com/a/3938151
  (into {} (remove (comp #(= "" %) second) m)))

(defn- remove-empties [big-map]
  (map remove-an-empty big-map))

(defn fix-coverurls [big-map]
  (into () (map #(if ((fnil clojure.string/includes? "unfortunately-no-cover-is-found") (:coverurl %) "http" ) % (assoc % :coverurl (str "http://libgen.io/covers/" (:coverurl %)))) big-map)))

(defn download-link [{:keys [md5 author title extension :as m]}]
  (str "http://libgen.io/get/" md5 "/" author "-" title "." extension))

(defn assoc-download-links [big-map]
  (into () (map #(assoc % :download-link (download-link %)) big-map)))

(defn search->big-map [search]
  (-> search
      do-sql
      remove-empties
      fix-coverurls
      assoc-download-links))


