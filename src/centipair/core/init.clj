(ns centipair.core.init
  (:require [centipair.core.channels :as core-channels]
            [taoensso.timbre :as timbre]))


(defn init-system []
  (do
    (core-channels/init-core-channels)))
