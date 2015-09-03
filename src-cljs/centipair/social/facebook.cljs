(ns centipair.core.social.facebook
  (:require [centipair.core.utilities.ajax :as ajax]
            [centipair.core.ui :as ui]
            [reagent.core :as reagent]))



(def fb-button-state (reagent/atom {:id "fb-button" :label "Login with facebook"}))


(defn fb-status-callback
  [response]
  
  )

(defn fb-check-login
  []
  (.getLoginStatus js/FB fb-status-callback))



(defn fb-login-callback
  [response]
  (.log js/console (aget response "authResponse" "accessToken")))


(defn fb-login
  []
  (.login js/FB fb-login-callback (clj->js {:scope  "public_profile,email"})))



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
