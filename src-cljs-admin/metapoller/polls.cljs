(ns metapoller.polls
  (:require [centipair.core.components.input :as input]
            [centipair.core.utilities.validators :as v]
            [centipair.core.ui :as ui]
            [centipair.core.utilities.ajax :as ajax]
            [centipair.core.utilities.spa :as spa]
            [reagent.core :as reagent]))

(def poll-id (reagent/atom {:id "poll-id" :type "hidden"} ))
(def poll-title (reagent/atom {:id "poll-title" :type "text" :label "Title" :validator v/required} ))
(def poll-hash-tag (reagent/atom {:id "poll-hash-tag" :type "text" :label "Twitter Hash Tag" :validator v/required}))
(def poll-description (reagent/atom {:id "poll-description" :type "textarea" :label "Description"}))

(def poll-form-state (reagent/atom {:title "Poll" :id "poll-form"}))


(defn poll-save []
  (ajax/form-post
   poll-form-state
   "/admin/api/polls"
   [poll-id poll-title poll-hash-tag poll-description]
   (fn [response] 
     (spa/redirect (str "/poll/edit/" (:poll_id response))))))



(def poll-save-button (reagent/atom {:label "Submit" :on-click poll-save :id "poll-save-button"}))

(defn poll-form []
  (input/form-aligned  
   poll-form-state
   [poll-title poll-hash-tag poll-description]
   poll-save-button))





(defn render-poll-form
  []
  (ui/render poll-form "content"))



(defn reset-poll-form
  []
  (input/reset-inputs [poll-id
                       poll-title
                       poll-description
                       poll-hash-tag]))

(defn new-poll-form
  []
  (reset-poll-form)
  (render-poll-form))


(defn map-poll-form
  [response]
  (do
    (input/update-value poll-id (:poll_id response))
    (input/update-value poll-title (:poll_title response))
    (input/update-value poll-description (:poll_description response))
    (input/update-value poll-hash-tag (:poll_hash_tag response))))


(defn edit-poll-form
  [id]
  (ajax/get-json (str "/admin/api/polls/" id)
                 {}
                 (fn [response]
                   (map-poll-form response)))
  (render-poll-form))


(defn list-polls [])
