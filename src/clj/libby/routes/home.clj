(ns libby.routes.home
  (:require [libby.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [redirect file-response]]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [libby.db :as db])
  (:import [java.io File FileInputStream FileOutputStream]))

(def resource-path "/tmp/")

(defn file-path [path & [filename]]
  (java.net.URLDecoder/decode
   (str path File/separator filename)
   "utf-8"))

(defn upload-file
  "uploads a file to the target folder
   when :create-path? flag is set to true then the target path will be created"
  [path {:keys [tempfile size filename]}]
  (let [_ (log/info tempfile size filename)]
  (try
    (with-open [in (new FileInputStream tempfile)
                out (new FileOutputStream (file-path path filename))]
      (let [source (.getChannel in)
            dest   (.getChannel out)]
        (.transferFrom dest source 0 (.size source))
        (.flush out))))))

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn results [query]
  (let [_ (log/info query)]
    (layout/render "results.html" {:query query :barf (db/search->big-map query)})))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/upload" [] (layout/render "upload.html"))
  (POST "/upload" [file]
    (upload-file resource-path file)
    (redirect (str "/files/" (:filename file))))
  (GET "/files/:filename" [filename]
    (file-response (str resource-path filename)))
  (GET "/" [] (home-page))
  (POST "/results" [query] (results query))
  (GET "/about" [] (about-page)))

