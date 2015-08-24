(ns centipair.core.utilities.dom
  (:require [goog.dom :as gdom]))

(defn get-element
  [id]
  (gdom/getElement id))

(defn get-value
  [id]
  (.-value ( gdom/getElement id)))


(defn set-value [id value]
  (set! (.-value ( gdom/getElement id)) value))


(defn innerHtml [id value]
  (set! (.-innerHTML ( gdom/getElement id)) value))
