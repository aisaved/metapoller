(ns centipair.core.auth.user.api
   (:use compojure.core)
   (:require [liberator.core :refer [resource defresource]]
             [centipair.core.contrib.response :as response]
             [centipair.core.auth.user.models :as user-models]))



(defresource api-user-register []
  :available-media-types ["application/json"]
  :allowed-methods [:post]
  :processable? (fn [context]
                  (user-models/validate-user-registration (:params (:request context))))
  :handle-unprocessable-entity (fn [context]
                                 (:validation-result context))
  :post! (fn [context] (user-models/register-user (:params (:request context))))
  :handle-created (fn [context] (response/liberator-json-response {:register "success"})))


(defresource api-user-login []
  :available-media-types ["application/json"]
  :allowed-methods [:post]
  :processable? (fn [context]
                  (user-models/check-login (:params (:request context))))
  :handle-unprocessable-entity (fn [context]
                                 (:validation-result context))
  :post! (fn [context]
           {:login-result (user-models/login (:params (:request context)))})
  :handle-created (fn [context]
                    (response/liberator-json-response-cookies 
                     (:login-result context)
                     {"auth-token" {:value (:auth-token (:login-result context))
                                    :max-age 86400
                                    :path "/"
                                    :http-only true}})))



(defresource admin-api-user [&[source]]
  :available-media-types ["application/json"]
  :allowed-methods [:post :get :delete :put]
  :processable? (fn [context] (if (= (:request-method (:request context)) :get)
                                true
                                (if (= (:request-method (:request context)) :delete)
                                  true
                                  (user-models/validate-admin-create-user (:params (:request context))))))
  ;;:exists? (fn [context] (if (nil? source) true (page-exists?  source)))
  :handle-unprocessable-entity (fn [context] (:validation-result context))
  :post! (fn [context]
           {:created (user-models/admin-save-user (:params (:request context)))})
  :handle-created (fn [context] (:created context))
  :delete! (fn [context]  (user-models/delete-user source))
  :delete-enacted? false
  :handle-ok (fn [context] (if (nil? source) 
                             (user-models/get-all-users (:params (:request context))) 
                             (user-models/get-user source))))

(defroutes api-user-routes
  (ANY "/admin/api/user" [] (admin-api-user))
  (ANY "/admin/api/user/:id" [id] (admin-api-user id))
  (POST "/api/register" [] (api-user-register))
  (POST "/api/login" [] (api-user-login)))
