(ns centipair.routes.home
  (:require [centipair.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [clojure.java.io :as io]
            [centipair.core.contrib.response :as response]
            [metapoller.models :as meta-models]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]))

(defn home-page []
  (layout/render
    "home.html"))


(defn fbconnect-page []
  (layout/render
    "fbconnect.html"))

(defn poll-page [hash-tag]
  (let [poll-data (meta-models/get-poll-hash hash-tag)]
    (if (nil? poll-data)
      (layout/render "404.html")
      (layout/render
            "poll.html" poll-data))))


(defn csrf-token []
  (response/json-response {:token *anti-forgery-token*}))



(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/poll/:hash-tag" [hash-tag] (poll-page hash-tag))
  (GET "/fbconnect" [] (fbconnect-page))
  (GET "/csrf" [] (csrf-token)))
