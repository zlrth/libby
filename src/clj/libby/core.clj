(ns libby.core
  (:require [libby.handler :as handler]
            [luminus.repl-server :as repl]
            [clj-http.client :as client]
            [luminus.http-server :as http]
            [libby.config :refer [env]]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [clojure.java.shell :refer :all]
            [clojure.string :as s]
            [mount.core :as mount])
  (:gen-class))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :parse-fn #(Integer/parseInt %)]])

(mount/defstate ^{:on-reload :noop}
                http-server
                :start
                (http/start
                  (-> env
                      (assoc :handler (handler/app))
                      (update :port #(or (-> env :options :port) %))))
                :stop
                (http/stop http-server))

(mount/defstate ^{:on-reload :noop}
                repl-server
                :start
                (when-let [nrepl-port (env :nrepl-port)]
                  (repl/start {:port nrepl-port}))
                :stop
                (when repl-server
                  (repl/stop repl-server)))




(defn stop-app []
  (doseq [component (:stopped (mount/stop))]
    (log/info component "stopped"))
  (shutdown-agents))

(defn start-app [args]
  (doseq [component (-> args
                        (parse-opts cli-options)
                        mount/start-with-args
                        :started)]
    (log/info component "started"))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

;; (:out (sh "node" "index.js"))

;; (sh "curl" (re-find #"http.*$" (:out (sh "node" "index.js"))))

;; (re-find #"get\.[^']+" (:out (sh "curl" (re-find #"http.*$" (:out (sh "node" "index.js"))))))

;; (str "http://libgen.io/" (re-find #"get\.[^']+" (:out (sh "curl" (re-find #"http.*$" (:out (sh "node" "index.js")))))))

;; (sh "wget" "-Ogood" (str "http://libgen.io/" (re-find #"get\.[^']+" (:out (sh "curl" (re-find #"http.*$" (:out (sh "node" "index.js"))))))))

(defn- fetch-binary!
  "makes an HTTP request and fetches the binary object"
  [url]
  (let [req (client/get url {:as :byte-array :throw-exceptions false})]
    (if (= (:status req) 200)
      (:body req))))


(defn change-query [name]
  (let [old (slurp "index.js")
        new (s/replace old #"jesus" name)]
    (spit "new.js" new)))

(defn get-results [query]
  (let [search-body (:body (client/get (str "http://libgen.io/search.php?req=" query)))
        ads (re-seq #"ads.php[^']+" search-body)
        ;; page-bodies (map #(:body (client/get (str "http://libgen.io/" %))) ads)
        ad (first ads)
        page-body (:body (client/get (str "http://libgen.io/" ad)))
        download-url (str "http://libgen.io/" (re-find #"get\.php[^']+"  page-body))
        ]
    download-url
    ;; (re-seq #"DOWNLOAD" page-body)
    ;; (count page-bodies)
    ))

(defn do-shit [name]
  (do
    (change-query name)
    (sh "wget" "-O" name ".epub" (str "http://libgen.io/" (re-find #"get\.[^']+" (:out (sh "curl" (re-find #"http.*$" (:out (sh "node" "new.js"))))))))))




(defn -main [& args]
  (start-app args))
