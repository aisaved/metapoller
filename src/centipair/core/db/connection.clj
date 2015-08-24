(ns centipair.core.db.connection
  (:use korma.db)
  (:require [centipair.core.config :as config]))



(defdb db (postgres config/sql-db-config))

