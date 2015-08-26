(ns metapoller.api
  (:use compojure.core)
  (:require [liberator.core :refer [resource defresource]]
            [centipair.core.contrib.response :as response]
            [metapoller.models :as poll-models]))


(defresource admin-api-polls [&[source]]
  :available-media-types ["application/json"]
  :allowed-methods [:post :get :delete :put]
  :processable? (fn [context] (if (= (:request-method (:request context)) :get)
                                true
                                (if (= (:request-method (:request context)) :delete)
                                  true
                                  (poll-models/validate-poll-create (:params (:request context))))))
  ;;:exists? (fn [context] (if (nil? source) true (page-exists?  source)))
  :handle-unprocessable-entity (fn [context] (:validation-result context))
  :post! (fn [context]
           {:created (poll-models/save-poll (:params (:request context)))})
  :handle-created (fn [context] (:created context))
  :delete! (fn [context]  (poll-models/delete-poll source))
  :delete-enacted? false
  :handle-ok (fn [context] (if (nil? source) 
                             (poll-models/get-all-polls (:params (:request context))) 
                             (poll-models/get-poll source))))


(defroutes admin-api-routes
  (ANY "/admin/api/polls" [] (admin-api-polls))
  (ANY "/admin/api/polls/:id" [id] (admin-api-polls id)))

