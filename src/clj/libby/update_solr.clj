(ns libby.update-solr
  (require [flux.core :as flux]
           [clojure.java.jdbc :as sql]))

(def mysql-db {:dbtype "mysql"
               :dbname "bookwarrior"
               :user "root"
               :password ""})

(defn mysql->solr [conn]
  (flux/with-connection conn (sql/query mysql-db
                                                      ["select id,author,lcc,md5,publisher,series,ddc,identifierwodash,doi,title,asin,pages,filesize,openlibraryid,edition,coverurl,extension,year from updated"]
                                                      {:row-fn flux/add}
                                                      )
    (flux/commit)))
