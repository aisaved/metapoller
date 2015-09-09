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
  :exists? (fn [context] (if (nil? source) true (poll-models/poll-exists?  source)))
  :handle-unprocessable-entity (fn [context] (:validation-result context))
  :post! (fn [context]
           {:created (poll-models/save-poll (:params (:request context)))})
  :handle-created (fn [context] (:created context))
  :delete! (fn [context]  
             (poll-models/delete-poll source))
  :delete-enacted? (fn [context] (if (nil? source)
                                   true
                                   (not (poll-models/poll-exists?  source))))
  
  :handle-ok (fn [context] (if (nil? source)
                             (response/liberator-json-response (poll-models/get-all-polls (:params (:request context))))
                             (response/liberator-json-response (poll-models/get-poll source)))))

(defresource api-user-poll [source]
  :available-media-types ["application/json"]
  :allowed-methods [:post]
  :processable? (fn [context] (poll-models/validate-user-poll source (:request context)))
  :exists? (fn [context] (if (nil? source) true (poll-models/poll-exists?  source)))
  :handle-unprocessable-entity (fn [context] (:validation-result context))
  :post! (fn [context]
               {:created (poll-models/user-poll-save source (:request context))})
  
  :handle-created (fn [context] (:created context)))


(defroutes admin-api-routes
  (POST "/api/poll/:id" [id] (api-user-poll id))
  (ANY "/admin/api/polls" [] (admin-api-polls))
  (ANY "/admin/api/polls/:id" [id] (admin-api-polls id)))

