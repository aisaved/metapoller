(ns metapoller.models
  (:use korma.db
        centipair.core.db.connection
        centipair.core.contrib.mail
        centipair.core.utilities.pagination)
  (:require
   [centipair.core.utilities.pagination :refer [offset-limit]]
   [validateur.validation :refer :all]
   [centipair.core.contrib.time :as t]
   [korma.core :as korma :refer [insert
                                 delete
                                 select
                                 where
                                 set-fields
                                 values
                                 fields
                                 offset
                                 limit
                                 defentity
                                 pk
                                 has-many
                                 join
                                 with]]))

(defentity poll)


(defn poll-exists?
  [poll-id]
  (not (empty? (select poll (where {:poll_id (Integer. poll-id)})))))


(defn unique-hashtag?
  [params]
  (let [hash-tag (:poll-hash-tag params)
        poll-obj (first (select poll (where {:poll_hash_tag hash-tag})))]
    (if (or (nil? poll-obj) (= (:poll-id params) (:poll_id poll-obj)))
      true
      [false {:validation-result {:errors {:poll-hash-tag "This hashtag already exists for another poll"}}}])))


(defn validate-poll-create
  [params]
  (let [v (validation-set (presence-of :poll-title)
                          (presence-of :poll-hash-tag))
        validation-result (v params)]
    (if (valid? validation-result)
      (unique-hashtag? params)
      [false {:validation-result {:errors validation-result}}])))


(defn update-poll
  [params]
  (korma/update poll (set-fields
                      {:poll_title (:poll-title params)
                       :poll_hash_tag (:poll-hash-tag params)
                       :poll_description (:poll-description params)})
                (where {:poll_id (Integer. (:poll-id params))}))
  {:poll_id (:poll-id params)})

(defn create-poll
  [params]
  (let [new-poll (insert poll (values {:poll_title (:poll-title params)
                        :poll_hash_tag (:poll-hash-tag params)
                        :poll_description (:poll-description params)
                        :poll_created_date (t/sql-time-now)}))]
    {:poll_id (:poll_id new-poll)}))


(defn save-poll
  [params]
  (if (nil? (:poll-id params))
    (create-poll params)
    (update-poll params)))


(defn delete-poll
  [poll-id]
  (delete poll (where {:poll_id (Integer. poll-id)})))


(defn get-poll
  [poll-id]
  (first (select poll (where {:poll_id (Integer. poll-id)}))))



(defn get-all-polls
  [params]
  (let [offset-limit-params (offset-limit (:page params) (:per params))
        total (count (select poll (fields :poll_id)))]
    {:result (select poll (fields :poll_hash_tag :poll_title :poll_id)
                     (offset (:offset offset-limit-params))
                     (limit (:limit offset-limit-params)))
     :total total
     :page (if (nil? (:page params)) 0 (Integer. (:page params)))}))
