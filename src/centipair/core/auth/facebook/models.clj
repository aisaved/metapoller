(ns centipair.core.auth.facebook.models
  (:use korma.db
        centipair.core.db.connection)
  (:require [korma.core :as korma
             :refer [insert
                     delete
                     select
                     where
                     set-fields
                     values
                     fields
                     offset
                     limit
                     defentity
                     pk
                     has-many
                     join
                     with]]
            [centipair.core.contrib.time :as t]
            [centipair.core.auth.user.models :as user-models]
            [clj-http.client :as client]
            [cheshire.core :refer [parse-string]]
            ))


(defentity facebook_account)
(def graph-api-url "https://graph.facebook.com/me")

(defn get-fb-user-info
  [access-token]
  (let [fb-response (client/get graph-api-url {:query-params {:access_token access-token
                                                              :fields "email,name"}
                                               :accept :json
                                               :throw-exceptions false})]
    (if (= 400 (:status fb-response))
      {:valid false}
      (if (= 200 (:status fb-response))
        (let [result (parse-string (:body fb-response) true)]
          {:valid true 
           :email (:email result)
           :id (:id result)
           :name (:name result)
           :access-token access-token})
        {:valid false :message "unknown error"}))))


(defn check-fb-login
  [access-token]
  (let [fb-user-info (get-fb-user-info access-token)]
    (:valid fb-user-info)))


(defn get-fb-account
  [fb-id]
  (if (nil? fb-id)
    nil
    (first (select facebook_account (where {:facebook_id fb-id})))))


(defn update-fb-account
  [fb-account]
  (user-models/update-password {:password (:facebook_access_token fb-account)
                                :user-id (:user_account_id fb-account)})
  (korma/update facebook_account (set-fields {:facebook_access_token (:facebook_access_token fb-account)})
                (where {:facebook_account_id (:facebook_account_id fb-account)})))

(defn create-fb-account
  [fb-user-info]
  (let [user-account (user-models/admin-save-user {:email (:email fb-user-info)
                                :password (:access-token fb-user-info)
                                :is-admin false
                                :active true})]
    (insert facebook_account (values {:user_account_id (:user_account_id user-account)
                                      :facebook_id (:id fb-user-info)
                                      :facebook_name (:name fb-user-info)
                                      :facebook_email (:email fb-user-info)
                                      :facebook_access_token (:access-token fb-user-info)}))))

(defn fb-login
  "Access token is used as password and email as username"
  [params]
  (let [fb-user-info (get-fb-user-info (:access-token params))
        fb-account (get-fb-account (:id fb-user-info))]
    (if (nil? fb-account)
      (let [new-fb-account (create-fb-account params)]
        (user-models/login {:username (:facebook_email new-fb-account)
                            :password (:access-token params)}))
      (do 
        (update-fb-account (assoc fb-account :facebook_access_token (:access-token params)))
        (user-models/login {:username (:facebook_email fb-account)
                            :password (:access-token params)})))))
