(ns centipair.app
  (:require [centipair.init :as init]
            [centipair.registry :as function-registry]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(init/init!)
