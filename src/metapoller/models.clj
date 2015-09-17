(ns metapoller.models
  (:use korma.db
        centipair.core.db.connection
        centipair.core.contrib.mail
        centipair.core.utilities.pagination)
  (:require
   [centipair.core.utilities.pagination :refer [offset-limit]]
   [validateur.validation :refer :all]
   [centipair.core.contrib.time :as t]
   [centipair.core.auth.user.models :as user-models]
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
                                 with

                                 aggregate
                                 ]]))

(defentity poll)
(defentity user_poll)
(defentity user_poll_log)
(defentity poll_stats)


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

(defn get-poll-hash
  [hash]
  (first (select poll (where {:poll_hash_tag hash}))))


(defn poll-exists?
  [poll-id]
  (not (nil? (get-poll poll-id))))


(defn poll-hash-exists?
  [hash]
  (not (nil? (get-poll-hash hash))))


(defn get-user-poll
  [poll-id user-id]
  (first (select user_poll (where {:poll_id (Integer. poll-id)
                                   :user_account_id user-id}))))


(defn get-user-poll-log
  [user-id]
  (first (select user_poll_log (where {:user_account_id user-id}))))

(defn get-all-polls
  [params]
  (let [offset-limit-params (offset-limit (:page params) (:per params))
        total (count (select poll (fields :poll_id)))]
    {:result (select poll (fields :poll_hash_tag :poll_title :poll_id)
                     (offset (:offset offset-limit-params))
                     (limit (:limit offset-limit-params)))
     :total total
     :page (if (nil? (:page params)) 0 (Integer. (:page params)))}))


(defn valid-vote?
  [poll-vote]
  (let [vote (Integer. poll-vote)]
    (if (nil? poll-vote)
      false
      (or (= 1 vote) (= -1 vote)))))

(defn valid-poll-interval?
  [poll-log]
  (if (t/time-expired? (:next_poll_time poll-log))
    true
    false))


(defn validate-user-poll
  [poll-id request]
  (let [user-account (user-models/get-authenticated-user request)
        poll-log (get-user-poll-log (:user_account_id user-account))
        user-poll (get-user-poll poll-id (:user_account_id user-account))]
    (if (nil? user-poll)
      (if (nil? poll-log)
        true
        (if (valid-poll-interval? poll-log)
          (if (valid-vote? (get-in request [:params :poll-vote]))
            true
            [false {:validation-result {:errors {:poll-id "Invalid vote"}}}])
          [false {:validation-result {:errors {:poll-id "You can only do one poll per hour"}}}]))
      [false {:validation-result {:errors {:poll-id "You have already voted for this poll"}}} ])))



(defn insert-user-poll
  [poll-id user-id poll-vote]
  (insert user_poll (values {:user_poll_id (Integer. poll-id)
                             :poll_id (Integer. poll-id)
                             :user_account_id user-id
                             :user_poll_vote (Integer. poll-vote)})))

(defn update-poll-stats
  [poll-id]
  (let [user-poll-stats (first(select
                               user_poll
                               (aggregate (sum :user_poll_vote) :user_poll_vote_total)
                               (aggregate (count :*)  :user_poll_count)
                               (where {:poll_id (Integer. poll-id)})))
        poll-count (or (:user_poll_count user-poll-stats) 0)
        poll-total (or (:user_poll_count user-poll-stats) 0)
        poll-points (if (> poll-count 0) (/ poll-total poll-count) 0)]
    (do (korma/insert poll_stats (values {:poll_id (Integer. poll-id)
                                          :poll_count poll-count
                                          :poll_total poll-total
                                          :poll_points poll-points}))
        (korma/update poll (set-fields {:poll_count poll-count
                                        :poll_total poll-total
                                        :poll_points poll-points})
                      (where {:poll_id (Integer. poll-id)})))))

(defn insert-user-poll-log
  [user-id]
  (do
    (delete user_poll_log (where {:user_account_id user-id}))
    (insert user_poll_log (values {:user_account_id user-id
                                   :next_poll_time (t/set-time-expiry 1)}))))


(defn user-poll-save
  [poll-id request]
  (let [user-account (user-models/get-authenticated-user request)
        poll-vote (get-in request [:params :poll-vote])]
    (let [user-poll (insert-user-poll poll-id (:user_account_id user-account) poll-vote)]
      (do
        (update-poll-stats poll-id)
        (insert-user-poll-log (:user_account_id user-account))
        {:poll-id poll-id}))))

(defn get-trending-polls
  []
  (select poll (limit 10)))


(defn to-high-charts
  [poll-stat]
  [(t/highchart-date-format (:poll_stats_time poll-stat)) (:poll_points poll-stat)])

(defn get-poll-stats
  [poll-id]
  (let [poll-data (get-poll poll-id)
        poll-stats (map to-high-charts (select poll_stats (where {:poll_id (Integer. poll-id)} )))]
    {:poll-data poll-data
     :poll-stats poll-stats}))
