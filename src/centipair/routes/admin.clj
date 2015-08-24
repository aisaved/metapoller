(ns centipair.routes.admin
  (:require [centipair.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [clojure.java.io :as io]
            [centipair.core.contrib.response :as response]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]))

(defn admin-home []
  (layout/render
    "admin/sbadmin.html"))



(defroutes admin-routes
  (GET "/admin" [] (admin-home)))
