(ns centipair.core.utilities.uuid
  (:require
    ;;[cljs-uuid.core :as uuid]
    [cljs-uuid-utils.core :as uuid]))




(defn make-random-uuid
  []
  (uuid/make-random-uuid))
