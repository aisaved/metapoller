(ns centipair.core.contrib.time
  (:require 
   [clj-time.core :as clj-time]
   [clj-time.local :as clj-time-local]
   [clj-time.coerce :as clj-time-coerce]
   [clj-time.format :as f]))


(defn set-time-expiry [hours]
  (clj-time-coerce/to-timestamp (clj-time/plus (clj-time-local/local-now) (clj-time/hours hours))))

(defn time-expired? [check-time]
  (let [session-time (clj-time-coerce/to-date-time check-time)
        expired? (clj-time/after? (clj-time-local/local-now) session-time)]
    expired?))

(defn expire-days [days]
  (clj-time/plus (clj-time-local/local-now) (clj-time/days days)))

(defn set-expire-days [days]
  (clj-time-coerce/to-sql-time (expire-days days)))


(defn cookie-expire-time [days]
  (f/unparse (f/formatters :rfc822) (clj-time/plus (clj-time/now) (clj-time/days days))))

(defn sql-time-now []
  (clj-time-coerce/to-sql-time (clj-time/now)))
