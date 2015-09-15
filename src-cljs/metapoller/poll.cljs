(ns metapoller.poll
  (:require [centipair.core.utilities.ajax :as ajax]
            [centipair.core.ui :as ui]
            [reagent.core :as reagent]
            [centipair.core.utilities.dom :as dom]))


(def poll-value-state (reagent/atom {:id "poll-value" :value 0}))
(def poll-buttons-state (reagent/atom {:disabled ""}))





(defn fill-poll-data
  [response]
  (.log js/console response)
  )



(defn get-poll-data
  []
  (let [poll-id (dom/get-value "poll-id")]
    (ajax/get-json (str "/api/poll/" poll-id)
              {}
              (fn [response] (fill-poll-data response)))))


(defn submit-poll
  [value]
  (let [poll-id (dom/get-value "poll-id")]
    (ajax/post (str "/api/poll/" poll-id)
               {:poll-id poll-id
                :poll-vote value}
               (fn [response] (.log js/console response)))))



(defn poll-buttons
  []
  [:div
   [:button {:type "button" :id "poll-positive" :class "btn btn-success" :disabled (:disabled @poll-buttons-state)
             :on-click (partial submit-poll 1)} "Positive"]
   [:button {:type "button" :id "poll-negative" :class "btn btn-danger" :disabled (:disabled @poll-buttons-state)
             :on-click (partial submit-poll -1)} "Negative"]])


(defn render-poll-buttons
  []
  (ui/render poll-buttons "poll-container"))


(defn render-poll-ui []
  (render-poll-buttons))
