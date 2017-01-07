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

(defn- fetch-binary!
  [url]
  (let [req (client/get url {:as :byte-array :throw-exceptions false})]
    (if (= (:status req) 200)
      (:body req))))

(defn get-results [query]
  (let [search-body (:body (client/get (str "http://libgen.io/search.php?req=" query)))
        ads (re-seq #"ads.php[^']+" search-body)
        ;; page-bodies (map #(:body (client/get (str "http://libgen.io/" %))) ads)
        ad (first ads)
        page-body (:body (client/get (str "http://libgen.io/" ad)))
        download-url (str "http://libgen.io/" (re-find #"get\.php[^']+"  page-body))
        ]
    download-url
    ))

(defn -main [& args]
  (start-app args))
