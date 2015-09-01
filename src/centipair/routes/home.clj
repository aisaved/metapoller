(ns centipair.routes.home
  (:require [centipair.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [clojure.java.io :as io]
            [centipair.core.contrib.response :as response]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]))

(defn home-page []
  (layout/render
    "home.html"))


(defn fbconnect-page []
  (layout/render
    "fbconnect.html"))


(defn csrf-token []
  (response/json-response {:token *anti-forgery-token*}))



(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/fbconnect" [] (fbconnect-page))
  (GET "/csrf" [] (csrf-token)))
