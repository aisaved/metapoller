(ns metapoller.polls
  (:require [centipair.core.components.input :as input]
            [centipair.core.utilities.validators :as v]
            [centipair.core.ui :as ui]
            [centipair.core.utilities.ajax :as ajax]
            [reagent.core :as reagent]))


(def poll-title (reagent/atom {:id "poll-title" :type "text" :label "Title" :validator v/required} ))
(def poll-hash-tag (reagent/atom {:id "poll-hash-tag" :type "text" :label "Twitter Hash Tag" :validator v/required}))

(def poll-form-state (atom {:title "Poll" :action "" :id "poll-form"}))


(defn poll-save []
  (.log js/console "Poll saved"))


(def poll-save-button (atom {:label "Submit" :on-click poll-save :id "poll-save-button"}))

(defn poll-form []
  (input/form-aligned  
   poll-form-state
   [poll-title poll-hash-tag]
   register-submit-button))


(defn render-poll-form
  []
  (ui/render poll-form "content"))
