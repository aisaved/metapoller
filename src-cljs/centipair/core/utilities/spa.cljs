(ns centipair.core.utilities.spa)


(defn home-page?
  "Checks whether the current page is home page
  if home page load feault componenets"
  []
  (if (or (= "" (.-hash js/location)) (nil? (.-hash js/location)))
    true false))



(defn redirect [hash-url]
  (set! (.-hash js/window.location) 
        (str hash-url)))
