(ns centipair.social.facebook
  (:require [centipair.core.utilities.ajax :as ajax]
            [centipair.core.ui :as ui]
            [reagent.core :as reagent]))



(def fb-button-state (reagent/atom {:id "fb-button" :label ""}))

(defn set-loggedin-button [user-info]
  (swap! fb-button-state assoc :label (:full_name user-info)))

(defn fb-status-callback
  [response]
  (.log js/console (aget response "status")))

(defn fb-check-login
  []
  (js/facebookCheckLogin fb-status-callback))



(defn fb-login-callback
  [response]
  (ajax/post "/api/facebook/login"
             {:access-token (aget response "authResponse" "accessToken")}
             (fn [response]
               (set-loggedin-button (:profile (:result response))))))


(defn fb-login
  []
  (js/facebookLogin fb-login-callback))



(defn fb-button
  []
  [:a {:class "btn btn-social btn-facebook"
       :on-click fb-login}
   [:i {:class "fa fa-facebook"}] (:label @fb-button-state)])




(defn set-login-button
  []
  (swap! fb-button-state assoc :label "Login with facebook"))


(defn render-fb-button
  []
  (ui/render fb-button "fb-container"))

(defn check-login-status
  []
  (ajax/get-json "/api/user/status" {:query "profile"}
            (fn [response]
              (if (:loggedin (:result response))
                (set-loggedin-button (:profile (:result response)))
                (set-login-button))
              (render-fb-button))))

(defn fb-init
  []
  (check-login-status))
