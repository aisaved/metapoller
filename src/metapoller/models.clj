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
   [centipair.core.contrib.cryptography :as crypto]
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
                                 order
                                 aggregate
                                 ]]))

(defentity poll)
(defentity user_poll)
(defentity user_poll_expire)
(defentity poll_stats)
(defentity poll_stats_expire)
(defentity expire_poll_stats)
(defentity twitter_account)
(defentity poll_tweet)



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
                                   :user_account_id user-id})
                 (order :user_poll_date :ASC))))



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



(defn valid-user-poll?
  [user-poll]
  (if (nil? user-poll)
    true
    (if (or (t/time-expired? (:next_poll_time user-poll)) (user-models/is-admin-id? (:user_account_id user-poll)))
      true
      false)))

(defn valid-twitter-vote?
  [tweet-params]
  (empty? (select poll_tweet (where {:poll_tweet_tweet_id (:tweet-id tweet-params)}))))


(defn validate-user-poll
  [poll-id request]
  (let [user-account (user-models/get-authenticated-user request)
        user-poll (get-user-poll poll-id (:user_account_id user-account))]
    (if (valid-user-poll? user-poll)
      (if (valid-vote? (get-in request [:params :poll-vote]))
        true
        [false {:validation-result {:errors {:poll-id "Invalid vote"}}}])
      [false {:validation-result {:errors {:poll-id "You can vote only once per hour"}}}])))



(defn insert-user-poll
  [poll-id user-id poll-vote]
  (do
    (insert user_poll (values {:poll_id (Integer. poll-id)
                               :user_account_id user-id
                               :user_poll_vote (Integer. poll-vote)
                               :next_poll_time (t/set-time-expiry 24)
                               }))
    (insert user_poll_expire (values {:poll_id (Integer. poll-id)
                                      :user_account_id user-id
                                      :user_poll_vote (Integer. poll-vote)
                                      :expire_time (t/set-time-expiry 24)}))))


(defn poll-stats-calc [poll-id]
  (first (select
          user_poll
          (aggregate (sum :user_poll_vote) :user_poll_vote_total)
          (aggregate (count :*)  :user_poll_count)
          (where {:poll_id (Integer. poll-id)}))))


(defn update-expire-stats
  "Updates stats for expire hour polls"
  [poll-id]
  (let [user-poll-stats (first (select
                                user_poll_expire
                                (aggregate (sum :user_poll_vote) :user_poll_vote_total)
                                (aggregate (count :*)  :user_poll_count)
                                (where {:poll_id (Integer. poll-id)
                                        :expire_time [< (t/sql-time-now)]})))
        poll-count (or (:user_poll_count user-poll-stats) 0)
        poll-total (or (:user_poll_vote_total user-poll-stats) 0)
        poll-points poll-total]
    (if (not (nil? user-poll-stats))
      (korma/insert poll_stats_expire (values {:poll_id (Integer. poll-id)
                                           :poll_count poll-count
                                           :poll_total poll-total
                                           :poll_points poll-points})))))


(defn update-poll-stats
  [poll-id]
  ;;TODO: dont update poll stats if number of polls is greater than number of updates
  (let [user-poll-stats (first (select
                                user_poll
                                (aggregate (sum :user_poll_vote) :user_poll_vote_total)
                                (aggregate (count :*)  :user_poll_count)
                                (where {:poll_id (Integer. poll-id)})))
        poll-positive-count (:user_poll_positive_count (first (select user_poll
                                                                  (aggregate (count :*)  :user_poll_positive_count)
                                                                  (where {:poll_id (Integer. poll-id) :user_poll_vote [> 0]}))))
        poll-negative-count (:user_poll_negative_count (first (select user_poll
                                                                  (aggregate (count :*)  :user_poll_negative_count)
                                                                  (where {:poll_id (Integer. poll-id) :user_poll_vote [< 0]}))))
        poll-count (or (:user_poll_count user-poll-stats) 0)
        poll-total (or (:user_poll_vote_total user-poll-stats) 0)
        poll-points poll-total]
    (println poll-positive-count)
    (println "----")
    (println poll-negative-count)
    (do (korma/insert poll_stats (values {:poll_id (Integer. poll-id)
                                          :poll_count poll-count
                                          :poll_total poll-total
                                          :poll_points poll-points}))
        (korma/insert poll_stats_expire (values {:poll_id (Integer. poll-id)
                                          :poll_count poll-count
                                          :poll_total poll-total
                                          :poll_points poll-points}))
        (korma/update poll (set-fields {:poll_count poll-count
                                        :poll_total poll-total
                                        :poll_points poll-points
                                        :poll_positive_count poll-positive-count
                                        :poll_negative_count poll-negative-count
                                        })
                      (where {:poll_id (Integer. poll-id)}))
        )))

(defn get-expired-polls
  []
  (let [expired-polls (select user_poll_expire
                              (where {:expire_time [< (t/sql-time-now)]}))]
    expired-polls))


(defn expire-poll-stats
  []
  (let [expired-polls (get-expired-polls)
        expired-poll-ids (map #(:poll_id %) expired-polls)]
    (if (not (empty? expired-poll-ids))
      (doseq [poll-id expired-poll-ids]
        (update-expire-stats poll-id)
        (delete user_poll_expire (where {:poll_id [in expired-poll-ids]}))))))




(defn user-poll-save
  [poll-id request]
  (let [user-account (user-models/get-authenticated-user request)
        poll-vote (get-in request [:params :poll-vote])]
    (let [user-poll (insert-user-poll poll-id (:user_account_id user-account) poll-vote)]
      (do
        (update-poll-stats poll-id)
        {:poll-id poll-id}))))

(defn get-trending-polls
  []
  (select poll (order :poll_points :DESC) (limit 10)))


(defn to-high-charts
  [poll-stat]
  [(t/highchart-date-format (:poll_stats_time poll-stat)) (:poll_points poll-stat)])



(defn get-poll-stats
  [poll-id &[update-poll poll-stats-id]]
  (let [poll-data (get-poll poll-id)
        poll-stats (if (and update-poll (not (= "null" poll-stats-id)) (not (nil? poll-stats-id)))
                     (select poll_stats (where {:poll_id (Integer. poll-id)
                                                :poll_stats_id [> (Integer. poll-stats-id)]})
                             (limit 10))
                     (select poll_stats (where {:poll_id (Integer. poll-id)})
                             (order :poll_stats_time :DESC)
                             (limit 10)))
        sorted-poll-stats (sort-by :poll_stats_time poll-stats)
        poll-stats-hc (map to-high-charts sorted-poll-stats)]
      {:poll-data poll-data
       :poll-stats poll-stats-hc
       :poll-stats-id (:poll_stats_id (if (and update-poll (not (= "null" poll-stats-id)) (not (nil? poll-stats-id)))
                                        (last poll-stats)
                                        (first poll-stats)
                                        ))}))


(defn get-poll-stats-expire
  [poll-id &[update-poll poll-stats-id]]
  (let [poll-data (get-poll poll-id)
        poll-stats (if (and update-poll (not (= "null" poll-stats-id)) (not (nil? poll-stats-id)))
                     (select poll_stats (where {:poll_id (Integer. poll-id)
                                                :poll_stats_id [> (Integer. poll-stats-id)]})
                             (limit 10))
                     (select poll_stats (where {:poll_id (Integer. poll-id)})
                             (order :poll_stats_time :DESC)))
        sorted-poll-stats (sort-by :poll_stats_time poll-stats)
        poll-stats-hc (map to-high-charts sorted-poll-stats)]
      {:poll-data poll-data
       :poll-stats poll-stats-hc
       :poll-stats-id (:poll_stats_id (if (and update-poll (not (= "null" poll-stats-id)) (not (nil? poll-stats-id)))
                                        (last poll-stats)
                                        (first poll-stats)
                                        )
                                      )}))

(defn get-poll-tweets
  [poll-id]
  (select poll_tweet (where {:poll_id poll-id})))


(defn create-or-get-twitter-user
  [tweet-params]
  (let [twitter-email (str (:user-id tweet-params) "@twitter.com") ;;fake twitter email
        user-account (user-models/select-user-email twitter-email)]
    (if (nil? user-account)
      (user-models/admin-save-user {:email twitter-email
                                    :password (crypto/random-base64 32)
                                    :is-admin false
                                    :active true})
      user-account)))


(defn get-tweet-polls
  [hash-tags]
  (let [tweet-poll (first (select poll (where {:poll_hash_tag
                                               [in hash-tags]})))]
    tweet-poll))


(defn save-tweet-poll
  [poll-id user-id tweet-params]
  (insert poll_tweet (values {:poll_id poll-id
                              :poll_tweet_vote (:vote tweet-params)
                              :poll_tweet_user_id user-id
                              :poll_tweet_tweet_id (:tweet-id tweet-params)
                              :poll_tweet_twitter_id (:user-id tweet-params)
                              :poll_tweet_text (:tweet-text tweet-params)
                              :poll_tweet_screen_name (:screen-name tweet-params)
                              :poll_tweet_profile_image (:profile-image tweet-params)
                              })))

(defn save-tweet-rating
  "tweet-params {:tweet-text (:text tweet)
                 :hash-tags (hash-tag-parser tweet-text)
                 :vote (rating-parser tweet-text)
                 :tweet-id (:id tweet)
                 :user-id (:id (:user tweet))
                 :screen-name (:screen_name (:user tweet))
                 :profile-image (:profile_image_url (:user tweet))}"
  [tweet-params]
  (let [user-account (create-or-get-twitter-user tweet-params)
        poll-obj (get-tweet-polls (:hash-tags tweet-params))]
    (if (not (nil? poll-obj))
      (let [user-poll (get-user-poll  (:poll_id poll-obj) (:user_account_id user-account))]
        (if (and
             (valid-user-poll? user-poll)
             (valid-vote? (:vote tweet-params))
             (valid-twitter-vote? tweet-params))
          (do
            (insert-user-poll (:poll_id poll-obj) (:user_account_id user-account) (:vote tweet-params))
            (save-tweet-poll (:poll_id poll-obj) (:user_account_id user-account) tweet-params)
            (update-poll-stats (:poll_id poll-obj))))))))


(defn get-home-page-poll
  []
  (let [home-poll (first (select poll (order :poll_points :DESC) (limit 1)))
        poll-stats (if (nil? home-poll) {} (get-poll-stats (:poll_id home-poll)))]
    poll-stats))
