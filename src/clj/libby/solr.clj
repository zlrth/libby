(ns libby.solr
  (:require [flux.http :as http]
           [flux.cloud :as cloud]
           [flux.core :as flux]))




(defn create-connection [{:keys [zk-connect url collection]}]
  (print zk-connect url collection)
  (if zk-connect
    (if collection
      (cloud/create zk-connect collection)
      (cloud/create zk-connect))
    (http/create url collection)))

(def config {:zk-connect "localhost:9983"
             :url "http://localhost:8983/solr"
             :collection "iaff"
             })

(def system
  "A Var containing an object representing the application under
  development."
  (atom {}))

(defn start
  "Starts the system running, updates the Var #'system."
  []
  (swap! system assoc :conn (create-connection @system)))

(defn stop
  "Stops the system if it is currently running, updates the Var
  #'system."
  []
  (when-let [conn (:conn @system)]
    (swap! system assoc :conn (.shutdown conn))))

(defn init
  "Creates and initializes the system under development in the Var
  #'system.
  We initialize the system to the value of the solr/config map."
  []
  (reset! system config))

(defn go
  "Initializes and starts the system running."
  []
  (init)
  (start)
  :ready)

(defn do-shit []
  (go))


(defn query "execute a query in Solr.
  Assumes that the system map already contains a connection that has been started."
  [system & args] (flux/with-connection (:conn system) (apply flux/query args)))

(defn q "Convenience method to send a query using the system in the #'system atom"
  [& args] (apply query @system args))

