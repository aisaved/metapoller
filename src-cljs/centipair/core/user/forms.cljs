(ns centipair.core.user.forms
  (:require [centipair.core.components.input :as input]
            [centipair.core.utilities.validators :as v]
            [centipair.core.utilities.dom :as dom]
            [centipair.core.ui :as ui]
            [centipair.core.components.notifier :as notifier]
            [centipair.core.utilities.ajax :as ajax]
            [reagent.core :as reagent :refer [atom]]))



(def registration-form-state (atom {:title "Sign Up" :action "/register-submit" :id "registration-form"}))
(def email (atom {:id "email" :type "email" :label "Email" :validator v/email-required} ))
(def username (atom {:id "username" :type "text" :label "Username" :validator v/required} ))
(def password (atom {:id "password" :type "password" :label "Password" :validator v/required}))
(def accept-box-terms (atom {:id "box-terms" :type "checkbox" :label "Terms & Conditions" :validator v/agree-terms :description "I've read and accept terms & conditions"}))

(defn password-required-match [field]
  (if (v/has-value? (:value field))
    (if (= (:value field) (:value @password))
      (v/validation-success)
      (v/validation-error "Passwords does not match"))
    (v/validation-error v/required-field-error)))

(def confirm-password (atom {:id "confirm-password" :type "password" :label "Confirm Password" :validator password-required-match}))

(defn register-submit []
  (ajax/form-post
   registration-form-state
   "/api/register"
   [username email password confirm-password]
   (fn [response] (.log js.console "yay!!!"))))

(def register-submit-button (atom {:label "Submit" :on-click register-submit :id "register-submit-button"}))

(defn registration-form []
  (input/form-aligned  
   registration-form-state
   [username email password confirm-password accept-box-terms]
   register-submit-button))


(defn render-register-form []
  (ui/render registration-form "register-form"))


(def login-form-state (atom {:title "Login" :action "/login-submit" :id "login-form"}))


(defn login-submit []
  (ajax/form-post
   login-form-state
   "/api/login"
   [username password]
   (fn [response] 
     (.replace js/window.location (:redirect response)))))

(def login-submit-button (atom {:label "Submit" :on-click login-submit :id "login-submit-button"}))

(defn login-form []
  (input/form-aligned login-form-state [username password] login-submit-button))

(defn render-login-form []
  (ui/render login-form "login-form"))




(def forgot-password-subheading (atom {:id "forgot-password-subheading" :label "Enter your email to reset password" :type "subheading"}))

(def forgot-password-form-state (atom {:title "Forgot password" :action "/login-submit" :id "forgot-password-form"}))


(defn forgot-password-success []
  (notifier/message "Please check your email for instructions on resetting password" "success"))

(defn render-forgot-password-success []
  (ui/render forgot-password-success "forgot-password-form"))


(defn forgot-password-submit
  []
  (ajax/form-post
   forgot-password-form-state
   "/forgot-password-submit"
   [email]
   (fn [response]
     (render-forgot-password-success))))

(def forgot-password-button (atom {:label "Submit" :on-click forgot-password-submit :id "forgot-password-button"}))

(defn forgot-password-form []
  (input/form-aligned
   forgot-password-form-state
   [forgot-password-subheading email]
   forgot-password-button))


(defn render-forgot-password-form []
  (ui/render forgot-password-form "forgot-password-form"))


(def password-reset-key (atom {:id "password-reset-key" :type "text" :value ""}))
(def reset-password-form-state (atom {:id "reset-password-form" :title "Reset password"}))

(defn reset-password-submit
  []
  (swap! password-reset-key assoc :value (dom/get-value "password-reset-key"))
  (ajax/form-post
   reset-password-form-state
   "/reset-password-submit"
   [password confirm-password password-reset-key]
   (fn [response]
     (.log js/console (clj->js response)))))

(def reset-password-button (atom {:id "reset-password-button" :type "button" :label "Submit" :on-click reset-password-submit}))

(defn reset-password-form []
  (input/form-aligned
   reset-password-form-state
   [password confirm-password]
   reset-password-button))

(defn render-reset-password-form []
  (ui/render reset-password-form "login-form"))
