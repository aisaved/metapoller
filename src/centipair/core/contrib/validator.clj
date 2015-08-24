(ns centipair.core.contrib.validator
  (:require [noir.validation :as v]))



(defn has-value?
  [value]
  (v/has-value? value))
