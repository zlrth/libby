(ns libby.db
  (:require [clojure.java.jdbc :as j]
            [clojure.core.memoize :as memo]))

(def mysql-db {:dbtype "mysql"
               :dbname "bookwarrior"
               :user "root"
               :password ""})

(defn do-sql [search]
  (j/query mysql-db ["select * from updated where title like ?" (str "%" search "%")]))

(defn get-all-unique-titles [search]
  (j/query mysql-db ["select title from updated"]))

;;(defn get-all-unique-titles
  ;;(j/query mysql-db ["select distinct title from updated"]))

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

(do-sql "tidying")
(def all-titles
  (map :title (get-all-unique-titles "tidying")))

(defn search->single-download-link [search]
  (-> search
      search->big-map
      first
      :download-link))

(declare min-edit-distance edit-distance)

(defn- edit-distance* [t p]
  (cond
   (empty? t) (count p)
   (empty? p) (count t)
   (= (last t) (last p)) (min-edit-distance t p 0)
   :else (min-edit-distance t p 1)))


(defn- min-edit-distance [t p cost]
  (min (+ (edit-distance (butlast t) p) 1)
       (+ (edit-distance t (butlast p)) 1)
       (+ (edit-distance (butlast t) (butlast p)) cost)))


(def edit-distance
  "Get minimum distance between two strings"
  (memo/lu edit-distance* :lu/threshold 50000))


(defn search [word lst & {:keys [rank] :or {rank 3}}]
  "Get a list of words based on the minimum distance"
  (sort-by #(edit-distance word %) < (filter #(<= (edit-distance word %) rank)
                                             lst)))

