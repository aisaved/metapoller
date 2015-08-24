(ns centipair.core.ui
  (:require
   [reagent.core :as reagent :refer [atom]]))


(defn render [elements root-id]
  (reagent/render
   [elements] 
   (. js/document (getElementById root-id))))


(defn cleared-element []
  [:span])

(defn clear [root-id]
  (reagent/render 
   [cleared-element] 
   (. js/document (getElementById root-id))))


(defn render-page-message [message]
  message)


;;page : {:title "title" :action-bar (define action bar from centipair.core.components.action) }
(defn define-page [page elements]
  [:div {:id "page-container"}
   [:h3 {:id "page-title"} (:title @page)]
   [:div {:id "page-message"} (render-page-message (:message @page))]
   [:div {:id "action-bar"} 
    (if (nil? (:action-bar @page))
      ""
      ((:action-bar @page)))]
   (elements)])

(defn render-page [page elements]
  (render (partial define-page page elements) "content"))
