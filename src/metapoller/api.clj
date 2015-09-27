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
  :allowed-methods [:post :get]
  :processable? (fn [context]
                  (if (= (:request-method (:request context)) :get)
                  true
                  (poll-models/validate-user-poll source (:request context))))
  :exists? (fn [context] (if (nil? source) true (poll-models/poll-exists?  source)))
  :handle-unprocessable-entity (fn [context] (:validation-result context))
  :post! (fn [context]
               {:created (poll-models/user-poll-save source (:request context))})
  
  :handle-created (fn [context] (:created context))
  :handle-ok (fn [context]
               (response/liberator-json-response (poll-models/get-poll source))))

(defresource api-poll-hash [hash]
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :exists? (fn [context] (if (nil? hash) true (poll-models/poll-hash-exists?  hash)))
  :handle-ok (fn [context] (response/liberator-json-response (poll-models/get-poll-hash hash))))

(defresource api-poll-stats [source]
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :exists? (fn [context] (poll-models/poll-exists? source))
  :handle-ok (fn [context]
               (if (get-in context [:request :params :poll-update])
                 (response/liberator-json-response (poll-models/get-poll-stats
                                                    source
                                                    (get-in context [:request :params :poll-update])
                                                    (get-in context [:request :params :poll-stats-id])))
                 (response/liberator-json-response (poll-models/get-poll-stats source)))))


(defresource api-home-poll []
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :exists? true
  :handle-ok (fn [context]
               (response/liberator-json-response (poll-models/get-home-page-poll))))



(defroutes admin-api-routes
  (ANY "/api/home/poll" [id] (api-home-poll))
  (ANY "/api/poll/stats/:id" [id] (api-poll-stats id))
  (ANY "/api/poll/hash/:hash" [hash] (api-poll-hash hash))
  (ANY "/private/api/poll/:id" [id] (api-user-poll id))
  (ANY "/admin/api/polls" [] (admin-api-polls))
  (ANY "/admin/api/polls/:id" [id] (admin-api-polls id)))

