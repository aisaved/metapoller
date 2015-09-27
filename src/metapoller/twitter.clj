(ns metapoller.twitter
  (:use
   [twitter.oauth]
   [twitter.callbacks]
   [twitter.callbacks.handlers]
   [twitter.api.streaming]
   [twitter.api.restful]
   [immutant.scheduling :refer :all]
   )
  (:require
   [http.async.client :as ac]
   [cheshire.core :refer [parse-string]]
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [metapoller.models :as meta-models]
   )
  (:import
   (twitter.callbacks.protocols AsyncStreamingCallback)))


(def my-creds (make-oauth-creds "ISVR5x4XjdwM4o5pZrOgK4pg0"
                                "DHJr1UgwTgejPnVn9Xe0qo1yeS9iD1jy2CsZFWpNDEqNwWWs4n"
                                "3246638502-OmUNOgBKq7saRPuB38lm6dql1rLiUUIxcHReFwS"
                                "3UVLg7LzHJ5CoIHLO2pH2cQ8OJbw2dbLuVkhXjzTZdvky"))


(defn hash-tag-parser
  [tweet-text]
  (filter
   (fn [each] (not (or (= each "#negative") (= each "#positive"))))
   (map #(clojure.string/replace % #"#" "") (into [] (re-seq  #"\B#\w*[a-zA-Z]+\w*" tweet-text)))))


(defn get-rating-value
  [rating-text]
  (if (nil? rating-text)
    0
    (let [rating-value (bigdec (clojure.string/replace rating-text #"#rating +" ""))]
      (if (> rating-value 10)
        (/ rating-value 10)
        rating-value))))

(defn hash-to-point
  [rating-hash]
  (case rating-hash
    "#positive" 1
    "#negative" -1
    nil 0))

(defn rating-parser
  [tweet-text]
  (let [lower-case-text (clojure.string/lower-case tweet-text)
        rating-hash (or (re-find #"#negative" lower-case-text) (re-find #"#positive" lower-case-text))
        ]
    (hash-to-point rating-hash)))



(defn process-tweet
  [tweet]
  (let [tweet-text (:text tweet)
        tweet-params {:tweet-text (:text tweet)
                      :hash-tags (hash-tag-parser tweet-text)
                      :vote (rating-parser tweet-text)
                      :tweet-id (:id tweet)
                      :user-id (:id (:user tweet))
                      :screen-name (:screen_name (:user tweet))
                      :profile-image (:profile_image_url (:user tweet))}]
    (meta-models/save-tweet-rating tweet-params)
    ))

(defn get-mentions []
  (statuses-mentions-timeline :oauth-creds my-creds :params {:count 1}))


(defn search [query]
  (search-tweets :oauth-creds my-creds :params {:q query :count 200}))


(defn search-mentions
  []
  ;;(try 
    (let [tweets (search "@metapoller")]
      (doseq [tweet (:statuses (:body tweets))] 
        (process-tweet tweet)))
    ;;(catch Exception e (str "Exception in twitter: " (.getMessage e))))
  )
