(ns libby.solr
  (:require [flux.core :as flux]
           [flux.embedded :as e]))

;; this atom holds the connection to the embedded solr server.
;; in solr terminology, it's called a core for some reason.
(def core (atom {}))

(defn connection-to-solr []
  (let [container (e/create-core-container "resources/solr" "resources/solr/solr.xml")
        core (e/create container :libbyname)] ;; sorry about the dumb name ":libbyname"
    core))

(defn start []
  (swap! core assoc :conn (connection-to-solr)))

(defn stop []
  (reset! core nil))

(defn query "execute a query in Solr.
  Assumes that the system map already contains a connection that has been started."
  [system & args] (flux/with-connection (:conn @core) (apply flux/query args)))

(defn q "Convenience method to send a query using the system in the #'system atom"
  [& args] (apply query (:conn @core) args))
