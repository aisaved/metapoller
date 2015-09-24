(ns metapoller.poll
  (:require [centipair.core.utilities.ajax :as ajax]
            [centipair.core.ui :as ui]
            [reagent.core :as reagent]
            [centipair.core.utilities.dom :as dom]
            [centipair.social.facebook :as fb]
            )
  (:use [centipair.core.components.notifier :only [notify]]))


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


(defn create-poll-chart
  "Return value must contain keys poll-stats and poll-data"
  [api-url container]
  (ajax/get-json api-url nil
                 (fn [response]
                   (.log js/console (clj->js response))
                   (js/createChart container
                                   (clj->js (:poll-data response))
                                   (clj->js (:poll-stats response))))))

(defn poll-chart
  []
  (create-poll-chart (str "/api/poll/stats/" (dom/get-value "poll-id")) "chart-container"))


(defn submit-poll
  [value]
  (let [poll-id (dom/get-value "poll-id")]
    (ajax/cpost (str "/private/api/poll/" poll-id)
               {:poll-id poll-id
                :poll-vote value}
               (fn [response] 
                 (notify 102 "Poll submitted")
                 (js/addPollData (clj->js {:poll_stats_time (js/Date.now), :poll_points value})))
               (fn [error] 
                 (case (:status error)
                   422 (notify 422 (get-in error [:response :errors :poll-id]))
                   403 (notify 403 "You have to login to perform this poll")
                   default (notify 403 "Some error occured"))))))



(defn poll-buttons
  []
  [:div {:class "text-center"}
   [:button {:type "button" :id "poll-positive" :class "btn btn-success poll-button" :disabled (:disabled @poll-buttons-state)
             :on-click (partial submit-poll 1)} "Positive"]
   [:button {:type "button" :id "poll-negative" :class "btn btn-danger poll-button" :disabled (:disabled @poll-buttons-state)
             :on-click (partial submit-poll -1)} "Negative"]])



(defn render-poll-buttons
  []
  (ui/render poll-buttons "poll-container"))


(defn render-poll-ui []
  (poll-chart)
  (render-poll-buttons)
  (fb/fb-init))


(defn render-home-page
  []
  (create-poll-chart "/api/home/poll" "chart-container")
  (fb/fb-init))
