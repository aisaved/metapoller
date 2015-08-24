(ns centipair.core.channels
  (:require [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]]
            [centipair.core.contrib.mail :as mail]))


(def mail-channel (chan))


(defn process-email
  [mail]
  (mail/send-mail mail))

(defn init-mail-channel
  []
  (go 
    (while true
      (process-email (<! mail-channel)))))


(defn send-async-mail
  [mail]
  (go (>! mail-channel mail)))



(defn init-core-channels
  []
  (do 
    (init-mail-channel)))

