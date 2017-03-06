(ns user
  (:require [mount.core :as mount]
            [libby.core :as libby]
            [libby.solr :as solr]
            [libby.update-solr :as u]
            [clojure.tools.logging :as log]
            [clj-time.periodic :as pt]
            [chime :as  chime]
            [clj-time.core :as t]
            [clojure.core.async :as a :refer [<! go-loop]]
            [libby.query :refer :all]))

(def f (clojure.java.io/file "tmp"))
(def fs (file-seq f))

(defn get-intervals [interval-secs]
  (pt/periodic-seq (t/now)
                   (-> interval-secs t/seconds)))
(defn schedule [f interval-secs]
  (chime/chime-at (get-intervals interval-secs) f))

(def g (atom 0))
(def all-contents (atom 0))

;; (def cancel (schedule (fn [e] (reset! g (rest (file-seq f)))) 1)) ;; every 1 seconds, reset the atom g to be the list of files in the tmp/ directory. to cancel the watch, eval (cancel)
;; slurp the first five files, combine their contents into another file, delete those files

;; every five seconds, concatenate the concents of the first five files in g.
;; (def slurper-cancel (schedule (fn [e] (reset! all-contents (apply str (map slurp (take 5 @g))))) 5))





(defn start []
  (mount/start-without #'libby.core/http-server
                       #'libby.core/repl-server))

(defn stop []
  (mount/stop-except #'libby.core/http-server
                     #'libby.core/repl-server))

(defn restart []
  (stop)
  (start))
