(ns metapoller.jobs
  (:require [immutant.scheduling :refer :all]
            [metapoller.twitter :as twitter]
            [metapoller.models :as meta-models]
            [taoensso.timbre :as timbre]))


(defn update-expire-chart
  []
  (timbre/info "Updating expire chart")
  (meta-models/expire-poll-stats)
  )

(defn update-twitter
  []
  ;;(timbre/info "Crawling twitter")
  (twitter/search-mentions))


(defn start-scheduler
  []
  (schedule update-twitter (every 10 :second))
  (schedule update-expire-chart
            (-> (in 5 :seconds)
                (every 1 :hour))))
