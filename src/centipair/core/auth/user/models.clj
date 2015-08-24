(ns centipair.core.auth.user.models
  "This provides an interface to various types of databases
  The user-model methods in this namesapce has to be implemented by the database system file"
  (:require [centipair.core.auth.user.sql :as user-model]
            [validateur.validation :refer :all]
            [centipair.core.utilities.validators :as v]
            [centipair.core.contrib.cookies :as cookies]))

;;Interface
(defn register-user
  [params]
  (user-model/register-user params))


(defn admin-save-user
  [params]
  (let [result (if (nil? (:user-id params))
                 (user-model/admin-create-user params)
                 (user-model/admin-update-user params))]
    {:user_account_id (:user_account_id result)}))


(defn login
  [params]
  (user-model/login params))


(defn password-reset-email
  [params]
  (user-model/password-reset-email params))


(defn select-user-email
  [value]
  (user-model/select-user-email value))


(defn activate-account
  [registration-key]
  (user-model/activate-account registration-key))


(defn check-login [params]
  (user-model/check-login params))


(defn get-user-session [auth-token]
  (user-model/get-user-session auth-token))


(defn get-authenticated-user
  [request]
  (let [auth-token (cookies/get-auth-token request)]
    (if (nil? auth-token)
      nil
      (get-user-session auth-token))))

(defn logged-in?
  [request]
  (if (nil? (get-authenticated-user request))
    false
    true))

;;validations
(defn email-exist-check
  [value]
  (if (v/has-value? value)
    (if (nil? (select-user-email value))
      true
      false)))


(def registration-validator
  (validation-set
   (presence-of :email :message "Your email address is required for registration")
   (presence-of :password :message "Please choose a password")
   (validate-by :email email-exist-check :message "This email already exists")))




(defn unique-email-validator [params]
  (let [user-account (select-user-email (:email params))]
    (if (nil? user-account)
      true
      (if (nil? (:user-id params))
        [false {:validation-result {:errors {:email ["This email already exists"]}}}]
        (if (= (:user_account_id user-account) (Integer. (:user-id params)))
          true
          [false {:validation-result {:errors {:email ["This email already exists"]}}}])))))


(def admin-create-user-validator
  (validation-set
   (presence-of :email :message "Your email address is required for registration")))


(defn validate-admin-create-user
  [params]
  (let [validation-result (admin-create-user-validator params)]
    (if (valid? validation-result)
      (unique-email-validator params)
      [false {:validation-result {:errors validation-result}}])))



(defn validate-user-registration
  [params]
  (let [validation-result (registration-validator params)]
    (if (valid? validation-result)
      true
      [false {:validation-result {:errors validation-result}}])))


(def login-validator
  (validation-set
   (presence-of :username :message "Please enter the email address you have registered.")
   (presence-of :password :message "Please enter your password")))


(defn validate-user-login
  [params]
  (let [validation-result (login-validator params)]
    (if (valid? validation-result)
      true
      [false {:validation-result {:errors validation-result}}])))


(defn delete-user 
  [user-id]
  (user-model/delete-user user-id))


(defn get-all-users
  [params]
  (user-model/get-all-users params))


(defn get-user
  [user-id]
  (user-model/get-user user-id))
