(ns centipair.handler
  (:require [compojure.core :refer [defroutes routes wrap-routes]]
            [centipair.routes.home :refer [home-routes]]
            [centipair.routes.admin :refer [admin-routes]]
            [metapoller.api :refer [admin-api-routes]]
            [metapoller.jobs :refer [start-scheduler]]
            [centipair.core.auth.user.routes :refer [user-routes]]
            [centipair.core.auth.user.api :refer [api-user-routes]]
            [centipair.core.auth.facebook.api :refer [api-facebook-routes]]
      
            [centipair.core.init :as init]
            
            [centipair.middleware :as middleware]
            [centipair.session :as session]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.3rd-party.rotor :as rotor]
            [selmer.parser :as parser]
            [environ.core :refer [env]]
            [clojure.tools.nrepl.server :as nrepl]))

(defonce nrepl-server (atom nil))

(defroutes base-routes
           (route/resources "/")
           (route/not-found "Not Found"))

(defn parse-port [port]
  (when port
    (cond
      (string? port) (Integer/parseInt port)
      (number? port) port
      :else          (throw (Exception. (str "invalid port value: " port))))))

(defn start-nrepl
  "Start a network repl for debugging when the :nrepl-port is set in the environment."
  []
  (when-let [port (env :nrepl-port)]
    (try
      (->> port
           (parse-port)
           (nrepl/start-server :port)
           (reset! nrepl-server))
      (timbre/info "nREPL server started on port" port)
      (catch Throwable t
        (timbre/error "failed to start nREPL" t)))))

(defn stop-nrepl []
  (when-let [server @nrepl-server]
    (nrepl/stop-server server)))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []

  (timbre/merge-config!
    {:level     (if (env :dev) :trace :info)
     :appenders {:rotor (rotor/rotor-appender
                          {:path "centipair.log"
                           :max-size (* 512 1024)
                           :backlog 10})}})

  (if (env :dev) (parser/cache-off!))
  (start-nrepl)
  ;;start the expired session cleanup job
  (session/start-cleanup-job!)
  (start-scheduler)
  (timbre/info (str
                 "\n-=[metapoller started successfully"
                 (when (env :dev) "using the development profile")
                 "]=-")))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "metapoller is shutting down...")
  (stop-nrepl)
  (timbre/info "shutdown complete!"))

(def app-base
  (routes
    (wrap-routes #'home-routes middleware/wrap-csrf)
    (wrap-routes #'api-user-routes middleware/wrap-csrf)
    (wrap-routes #'user-routes middleware/wrap-csrf)
    ;;(wrap-routes #'home-routes middleware/wrap-csrf)
    (wrap-routes #'admin-routes middleware/wrap-csrf)
    (wrap-routes #'admin-api-routes middleware/wrap-csrf)
    (wrap-routes #'api-facebook-routes middleware/wrap-csrf)
    #'base-routes))

(def app (middleware/wrap-base #'app-base))
