(ns user
  (:require [mount.core :as mount]
            [libby.core :as libby]
            [libby.solr :as solr]
            [libby.update-solr :as u]
            [clojure.tools.logging :as log]
            [clj-time.periodic :as pt]
            [chime :as  chime]
            [clj-time.core :as t]
            [clojure.core.async :as a :refer [>! <! >!! <!! go chan buffer close! thread
                                              alts! alts!! timeout]]
            [clojure.java.io :refer :all]
            [libby.query :refer :all]))

(def f (clojure.java.io/file "tmp"))
(def dest-files (clojure.java.io/file "dest"))
(def fs (file-seq f))
(def ds (file-seq dest-files))

(defn get-intervals [interval-secs]
  (pt/periodic-seq (t/now)
                   (-> interval-secs t/seconds)))
(defn schedule [f interval-secs]
  (chime/chime-at (get-intervals interval-secs) f))

(def g (atom 0))
(def all-contents (atom 0))

(def counter (atom 0))

#_(def file-creator (schedule (fn [e]
                              (spit (str "dest/" "file" @counter) (str @counter "\n"))
                              (swap! counter inc))
                            1))
;; 
#_(let [result (future (let [five (take 5 (rest (file-seq dest-files)))]
                       (spit "processed/flubber.txt"
                             (apply str (map slurp five))
                             :append true)
                       (map #(.delete %) five)))]
  @result)



#_(def file-lister (schedule (fn [e] (reset! g (rest (file-seq dest-files)))) 1)) ;; every 1 seconds, reset the atom g to be the list of files in the tmp/ directory. to cancel the watch, eval (cancel)
;; slurp the first five files, combine their contents into another file, delete those files

;; every five seconds, concatenate the concents of the first five files in g.
;; (def slurper-cancel (schedule (fn [e] (reset! all-contents (apply str (map slurp (take 5 @g))))) 5))

#_(def file-processor (schedule (fn [e]
                                (let [five (take 5 @g)]
                                  (spit "processed/flubber.txt"
                                        (apply str (map slurp five)) :append true)
                                  (println five)
                                  (doall (map #(.delete %) five)))) 4))




(defn start []
  (mount/start-without #'libby.core/http-server
                       #'libby.core/repl-server))

(defn stop []
  (mount/stop-except #'libby.core/http-server
                     #'libby.core/repl-server))

(defn restart []
  (stop)
  (start))


(defn append-to-file
  "Write a string to the end of a file"
  [filename s]
  (spit filename s :append true))

(defn format-quote
  "Delineate the beginning and end of a quote because it's convenient"
  [quote]
  (str "=== BEGIN QUOTE ===\n" quote "=== END QUOTE ===\n\n"))

(defn random-quote
  "Retrieve a random quote and format it"
  []
  (format-quote (slurp "http://www.braveclojure.com/random-quote")))

(defn do-stuff
  []
  (Thread/sleep 5000)
  (str (java.util.UUID/randomUUID) "\n"))

(defn snag-quotes
  [filename num-quotes]
  (let [c (chan)]
    (go (while true (append-to-file filename (<! c))))
    (dotimes [n num-quotes] (go (>! c (do-stuff))))))

;; in a blocking thread, take ten of those files, concat their contents, and append the result to a log file.




;; take a set of files, and put them on the queue.

;; put the message (do-stuff) on the queue, where (do-stuff) is (do (sleep 5) "stuff")

;; then take it off the queue with (append-to-file "log.log" (<! c))
