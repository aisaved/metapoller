(ns metapoller.jobs
  (:require [immutant.scheduling :refer :all]
            [metapoller.twitter :as twitter]
            [taoensso.timbre :as timbre]))



(defn update-twitter
  []
  (timbre/info "Crawling twitter")
  (twitter/search-mentions))


(defn start-scheduler
  []
  (schedule update-twitter (every 10 :second)))
