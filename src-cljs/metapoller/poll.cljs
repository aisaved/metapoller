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


(def live-poll-stats (reagent/atom nil))
(def live-poll-stats-expire (reagent/atom nil))


(defn percent [value total]
  (.round js/Math (* 100 (/ value total))))


(defn poll-stats-ui []
  (let [positive-percent (percent (:poll_positive_count (:poll-data @live-poll-stats)) (:poll_count (:poll-data @live-poll-stats)))
        negative-percent (percent (:poll_negative_count (:poll-data @live-poll-stats)) (:poll_count (:poll-data @live-poll-stats)))]
    [:div {:class "percent-chart-container"}
     [:div {:class "poll-chart chart-positive" :style {:width (str positive-percent "%")}} (str "Positive votes " positive-percent "%") ]
     [:div {:class "poll-chart chart-negative" :style {:width (str negative-percent "%")}} (str "Negative votes " negative-percent "%")]]))

(defn render-poll-stats-ui []
  (ui/render poll-stats-ui "poll-stats-container"))


(defn create-expire-poll-chart
  "Return value must contain keys poll-stats and poll-data"
  [api-url container]
  (.log js/console "Loading expire")
  (ajax/get-json api-url nil
                 (fn [response]
                   (js/createHistoryChart container
                                   (clj->js (:poll-data response))
                                   (clj->js (:poll-stats response)))
                   (reset! live-poll-stats-expire response)
                   )))


(defn create-poll-chart
  "Return value must contain keys poll-stats and poll-data"
  [api-url container]
  (ajax/get-json api-url nil
                 (fn [response]
                   (js/createChart container
                                   (clj->js (:poll-data response))
                                   (clj->js (:poll-stats response)))
                   (reset! live-poll-stats response)
                   (render-poll-stats-ui)
                   (create-expire-poll-chart (str "/api/poll/stats/expire/" (:poll_id (:poll-data response))) "history-chart-container")
                   )))

(defn poll-chart
  []
  (create-poll-chart (str "/api/poll/stats/" (dom/get-value "poll-id")) "chart-container"))


(defn expire-chart
  []
  (create-expire-poll-chart (str "/api/poll/stats/expire/" (dom/get-value "poll-id")) "expire-chart-container"))


(defn update-poll-chart
  []
  (if (nil? @live-poll-stats)
    (.log js/console "Poll stats not updated")
    (ajax/bget-json
       (str "/api/poll/stats/" (:poll_id (:poll-data @live-poll-stats)))
       {:poll-update true
        :poll-stats-id (:poll-stats-id @live-poll-stats)}
       (fn [response]
         (if (not (empty? (:poll-stats response)))
           (do
             (doseq [each-poll-stats (:poll-stats response)]
               (js/addPollData (clj->js {:poll_stats_time (first each-poll-stats), :poll_points (second each-poll-stats)})))
             (reset! live-poll-stats response)))))))


(defn update-poll-chart-expire
  []
  (if (nil? @live-poll-stats-expire)
    (.log js/console "Poll stats not updated")
    (ajax/bget-json
       (str "/api/poll/stats/expire/" (:poll_id (:poll-data @live-poll-stats-expire)))
       {:poll-update true
        :poll-stats-id (:poll-stats-id @live-poll-stats-expire)}
       (fn [response]
         (if (not (empty? (:poll-stats response)))
           (do
             (doseq [each-poll-stats (:poll-stats response)]
               (js/addHistoryChart (clj->js {:poll_stats_time (first each-poll-stats), :poll_points (second each-poll-stats)})))
             (reset! live-poll-stats-expire response)))))))


(defn submit-poll
  [value]
  (let [poll-id (:poll_id (:poll-data @live-poll-stats))]
    (ajax/cpost (str "/private/api/poll/" poll-id)
               {:poll-id poll-id
                :poll-vote value}
               (fn [response] 
                 (notify 102 "Poll submitted")
                 (update-poll-chart))
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




(defn start-live-chart
  []
  (js/setInterval update-poll-chart 3000)
  (js/setInterval update-poll-chart-expire 3000))


(defn render-poll-ui []
  (poll-chart)
  (render-poll-buttons)
  ;;(render-poll-stats-ui)
  (fb/fb-init)
  (start-live-chart))


(defn render-home-page
  []
  (create-poll-chart "/api/home/poll" "chart-container")  
  (fb/fb-init)
  (render-poll-buttons)
  (start-live-chart))
