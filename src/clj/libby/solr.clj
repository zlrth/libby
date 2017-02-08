(ns libby.solr
  (:require [flux.http :as http]
           [flux.cloud :as cloud]
           [flux.core :as flux]
           [flux.embedded :as e]))


(defn setup []
  (let [container (e/create-core-container "resources/solr" "resources/solr/solr.xml")
        core (e/create container :libbyname)]
    core))

(def core (setup))


(defn create-connection [{:keys [zk-connect url collection]}]
  (print zk-connect url collection)
  (if zk-connect
    (if collection
      (cloud/create zk-connect collection)
      (cloud/create zk-connect))
    (http/create url collection)))

(defn query "execute a query in Solr.
  Assumes that the system map already contains a connection that has been started."
  [system & args] (flux/with-connection core (apply flux/query args)))

(defn q "Convenience method to send a query using the system in the #'system atom"
  [& args] (apply query core args))

