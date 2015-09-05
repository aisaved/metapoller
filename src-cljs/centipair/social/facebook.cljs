(ns centipair.core.social.facebook
  (:require [centipair.core.utilities.ajax :as ajax]
            [centipair.core.ui :as ui]
            [reagent.core :as reagent]))



(def fb-button-state (reagent/atom {:id "fb-button" :label "Login with facebook"}))


(defn fb-status-callback
  [response]
  (.log js/console (aget response "status")))

(defn fb-check-login
  []
  (js/facebookCheckLogin fb-status-callback))



(defn fb-login-callback
  [response]
  (.log js/console (aget response "authResponse" "accessToken"))
  (ajax/post "/api/facebook/login"
             {:access-token (aget response "authResponse" "accessToken")}
             (fn [response] (.log js/console response))))


(defn fb-login
  []
  (js/facebookLogin fb-login-callback))



(defn fb-button
  []
  [:a {:class "btn btn-social btn-facebook"
       :on-click fb-login}
   [:i {:class "fa fa-facebook"}] (:label @fb-button-state)])


(defn render-fb-button
  []
  (ui/render fb-button "fb-container"))



(defn fb-init
  []
  (render-fb-button))
