(ns libby.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [libby.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[libby started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[libby has shut down successfully]=-"))
   :middleware wrap-dev})
