(ns metapoller.models
  (:use korma.db
        centipair.core.db.connection
        centipair.core.contrib.time
        centipair.core.contrib.mail
        centipair.core.utilities.pagination)
  (:require
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


(defn validate-poll-create
  [params])


(defn save-poll
  [params])


(defn delete-poll
  [poll-id])

(defn get-poll
  [poll-id])

(defn get-all-polls [params])
