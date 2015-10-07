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
            [centipair.core.contrib.cryptography :as crypto]
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
  
  (korma/update facebook_account (set-fields {:facebook_access_token (:facebook_access_token fb-account)})
                (where {:facebook_account_id (:facebook_account_id fb-account)})))


(defn create-or-get-user-account
  [email]
  (let [user-account (user-models/select-user-email email)]
    (if (nil? user-account)
      (user-models/admin-save-user {:email email
                                    :password (crypto/random-base64 32)
                                    :is-admin false
                                    :active true})
      user-account)))

(defn create-fb-account
  [fb-user-info]
  (let [user-account (create-or-get-user-account (if (nil? (:email fb-user-info))
                                                   (str (:id fb-user-info) "@facebook.com")
                                                   (:email fb-user-info)))]
    (insert facebook_account (values {:user_account_id (:user_account_id user-account)
                                      :facebook_id (:id fb-user-info)
                                      :facebook_name (:name fb-user-info)
                                      :facebook_email (:email fb-user-info)
                                      :facebook_access_token (:access-token fb-user-info)}))))


(defn fb-login
  [access-token]
  (let [fb-user-info (get-fb-user-info access-token)
        fb-account (get-fb-account (:id fb-user-info))]
    (println fb-user-info)
    (if (nil? fb-account)
      (let [new-fb-account (create-fb-account fb-user-info)]
        (user-models/create-fb-user-profile new-fb-account)
        (user-models/simulate-user-login (:user_account_id new-fb-account)))
      (do 
        (update-fb-account (assoc fb-account :facebook_access_token access-token))
        (user-models/create-fb-user-profile fb-account)
        (user-models/simulate-user-login (:user_account_id fb-account))))))
