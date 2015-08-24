(ns centipair.core.components.notifier
  (:require [reagent.core :as reagent :refer [atom]]))


(def notifier-state (atom {:class "notify" :text ""}))

(defn notifier-component []
  [:div {:class (:class @notifier-state)} (:text @notifier-state)])

(defn render-notifier-component []
  (reagent/render
   [notifier-component]
   (. js/document (getElementById "notifier"))))


(defn notify [code & [message]]
    (case code
      201 (reset! notifier-state {:class "notify" :text ""})
      200 (reset! notifier-state {:class "notify" :text ""})
      102 (reset! notifier-state {:class "notify notify-loading" :text (if (nil? message) "Loading" message)})
      404 (reset! notifier-state {:class "notify notify-error" :text (if (nil? message) "Resource unavailable" message)})
      500 (reset! notifier-state {:class "notify notify-error" :text (if (nil? message) "Internal Server Error" message)})
      422 (reset! notifier-state {:class "notify notify-error" :text (if (nil? message) "Unprocessable entity" message)})
      401 (reset! notifier-state {:class "notify notify-error" :text (if (nil? message) "Not Authorized" message)})
      403 (reset! notifier-state {:class "notify notify-error" :text (if (nil? message) "Access Denied" message)})
      405 (reset! notifier-state {:class "notify notify-error" :text (if (nil? message) "Method not allowed" message)})
      (reset! notifier-state {:class "notify" :text ""})))


(defn message
  [message type]
  [:div {:class (str "alert alert-" type) :role "alert"} message])
