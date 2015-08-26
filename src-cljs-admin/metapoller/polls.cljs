(ns metapoller.polls
  (:require [centipair.core.components.input :as input]
            [centipair.core.utilities.validators :as v]
            [centipair.core.ui :as ui]
            [centipair.core.utilities.ajax :as ajax]
            [reagent.core :as reagent]))


(def poll-title (reagent/atom {:id "poll-title" :type "text" :label "Title" :validator v/required} ))
(def poll-hash-tag (reagent/atom {:id "poll-hash-tag" :type "text" :label "Twitter Hash Tag" :validator v/required}))
(def poll-description (reagent/atom {:id "poll-description" :type "textarea" :label "Description"}))

(def poll-form-state (reagent/atom {:title "Poll" :id "poll-form"}))


(defn poll-save []
  (ajax/form-post
   poll-form-state
   "/api/admin/poll"
   [poll-title poll-hash-tag poll-description]
   (fn [response] (.log js.console "yay!!!"))))



(def poll-save-button (reagent/atom {:label "Submit" :on-click poll-save :id "poll-save-button"}))

(defn poll-form []
  (input/form-aligned  
   poll-form-state
   [poll-title poll-hash-tag poll-description]
   poll-save-button))





(defn render-poll-form
  []
  (ui/render poll-form "content"))
