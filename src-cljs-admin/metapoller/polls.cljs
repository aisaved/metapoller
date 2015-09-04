(ns metapoller.polls
  (:require [centipair.core.components.input :as input]
            [centipair.core.utilities.validators :as v]
            [centipair.core.ui :as ui]
            [centipair.core.utilities.ajax :as ajax]
            [centipair.core.utilities.spa :as spa]
            [centipair.core.components.table :refer [data-table generate-table-rows]]
            [centipair.core.components.notifier :refer [notify]]
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
     (notify 102 "Poll saved")
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


(defn poll-headers
  []
  [:tr
   [:th "Title"]
   [:th "Hash Tag"]
   [:th "Action"]
   ])

(defn delete-poll [id]
  (ajax/delete
   (str "/admin/polls/" id)
   (fn [response]
     
     )))


(def poll-data (reagent/atom {:page 0
                      :id "admin-polls-table"
                      :url "poll/list"
                      :total 0
                      :rows [:tr [:td "Loading"]]
                      :headers (poll-headers)
                      :create {:entity "poll"} 
                      :delete {:action (fn [] (.log js/console "delete"))}
                      :id-field "poll_id"
                      :per 50
                      }))

(declare load-polls)

(defn refresh-poll-list
  []
  (load-polls (:page @poll-data)))

(defn poll-row [row-data]
  [:tr {:key (str "table-row-" ((keyword (:id-field @poll-data)) row-data)) :class "clickable"}
   [:td {:key (str "table-column-1-" ((keyword (:id-field @poll-data)) row-data))}
    [:a {:href (str "#/poll/edit/" (:poll_id row-data)) :key (str "edit-link-" (:poll_id row-data)) } (:poll_title row-data)]]
   [:td {:key (str "table-column-2-" ((keyword (:id-field @poll-data)) row-data))} (str (:poll_hash_tag row-data))]
   [:td {:key (str "table-column-3-" ((keyword (:id-field @poll-data)) row-data))}
    [:a {:href "javascript:void(0)" 
         :on-click #(ajax/delete-entity {:url (str "/admin/api/polls/" (:poll_id row-data))
                                        :callback refresh-poll-list})
         :class "fa fa-trash-o"
         :title "Delete"
         :key (str "row-delete-link-" ((keyword (:id-field @poll-data)) row-data)) }]]])

(defn create-poll-data-list []
  (data-table poll-data))


(defn load-polls [page]
  (swap! poll-data assoc :page (js/parseInt page))
  (ajax/get-json 
   (str "/admin/api/polls")
   {:page (:page @poll-data)
    :per (:per @poll-data)}
   (fn [response]
     (generate-table-rows response poll-data poll-row))))


(defn render-poll-list [page]
  (ui/render create-poll-data-list "content")
  (load-polls page))
