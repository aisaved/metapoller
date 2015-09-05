(ns centipair.core.auth.facebook.api
  (:use compojure.core)
   (:require [liberator.core :refer [resource defresource]]
             [centipair.core.contrib.response :as response]
             [centipair.core.auth.facebook.models :as fb-models]))




(defresource api-fb-login []
  :available-media-types ["application/json"]
  :allowed-methods [:post]
  :processable? (fn [context]
                  (fb-models/check-fb-login (get-in context [:request :params "access-token"])))
  :handle-unprocessable-entity (fn [context]
                                 (:validation-result context))
  :post! (fn [context]
           {:login-result (fb-models/fb-login (get-in context [:request :params "access-token"]))})
  :handle-created (fn [context]
                    (println (:login-result context))
                    (response/liberator-json-response-cookies 
                     (:login-result context)
                     {"auth-token" {:value (:auth-token (:login-result context))
                                    :max-age 86400
                                    :path "/"
                                    :http-only true}})))

(defroutes api-facebook-routes
  (POST "/api/facebook/login" [] (api-fb-login)))
