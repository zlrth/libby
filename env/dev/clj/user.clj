(ns user
  (:require [mount.core :as mount]
            [libby.core :as libby]
            [libby.solr :as solr]
            [libby.update-solr :as u]
            [clojure.tools.logging :as log]))

(defn start []
  (mount/start-without #'libby.core/http-server
                       #'libby.core/repl-server))

(defn stop []
  (mount/stop-except #'libby.core/http-server
                     #'libby.core/repl-server))

(defn restart []
  (stop)
  (start))
