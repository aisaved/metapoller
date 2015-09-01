(ns centipair.core.utilities.spa
  (:require [centipair.core.ui :as ui]
            [centipair.core.utilities.ajax :as ajax]
            [reagent.core :as reagent]))


(defn home-page?
  "Checks whether the current page is home page
  if home page load feault componenets"
  []
  (if (or (= "" (.-hash js/location)) (nil? (.-hash js/location)))
    true false))



(defn redirect [hash-url]
  (set! (.-hash js/window.location) 
        (str hash-url)))

