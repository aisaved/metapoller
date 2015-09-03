(ns centipair.core.auth.facebook
  (:use korma.db
        centipair.core.db.connection)
  (:require [korma.core :as korma
             :refer [insert
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
                     with]]
            [centipair.core.contrib.time :as t]
            [centipair.core.auth.user.models :as user-models]))


(defentity facebook_account)
