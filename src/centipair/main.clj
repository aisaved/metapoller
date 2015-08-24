(ns centipair.main
  (:require [centipair.handler :refer [app init destroy]]
            [immutant.web :as immutant]
            
            [ring.middleware.reload :as reload]
            [taoensso.timbre :as timbre]
            [environ.core :refer [env]])
  (:gen-class))

(defn parse-port [[port]]
  (Integer/parseInt (or port (env :port) "3000")))




(defonce server (atom nil))

(defn start-server [args]
  "Args should be a flat sequence of key/value pairs corresponding to
  options accepted by `immutant.web/run`. Keys may be keywords or
  strings, but the latter should not include the colon prefix. If the
  :dev key is present in the environment, `immutant.web/run-dmc` will be used"
  (init)
  (reset! server
          (if (env :dev)
            (immutant/run-dmc #'app args)
            (immutant/run app args))))

(defn stop-server []
  (when @server
    (destroy)
    (immutant/stop @server)
    (reset! server nil)))

(defn start-app [args]
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-server))
  (start-server args)
  (timbre/info "server started on port:" (:port @server)))





(defn -main [& args]
  (start-app args))

