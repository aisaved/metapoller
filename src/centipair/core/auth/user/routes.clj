(ns centipair.core.auth.user.routes
  (:require [compojure.core :refer :all]
            [centipair.layout :as layout]
            [centipair.core.auth.user.models :as user-model]))


(defn register-page
  "Registration Page"
  []
  (layout/render "core/user/register.html"))

(defn login-page
  "Login Page"
  []
  (layout/render "core/user/login.html"))

(defn activate
  "Account activation handler"
  [registration-key]
  (let [user-id (user-model/activate-account registration-key)]
  (if user-id
    (layout/render "core/user/login.html" {:title "Account activated" :message "Your account has been activated.Please Login"})
    (layout/render "core/user/account-activation.html" {:title "Account activation error" :message "Invalid activation code"}))))



(defroutes user-routes 
  (GET "/register" [] (register-page))
  (GET "/login" [] (login-page))
  (GET "/activate/:key" [key] (activate key)))
