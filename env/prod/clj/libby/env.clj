(ns libby.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[libby started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[libby has shut down successfully]=-"))
   :middleware identity})
