(ns centipair.admin.channels
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [put! chan <!]]
            [centipair.core.utilities.ajax :as ajax]
            [centipair.core.ui :as ui]))


