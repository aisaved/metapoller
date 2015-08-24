(ns centipair.core.csrf
  (:require [centipair.core.utilities.ajax :as ajax]
            [centipair.core.utilities.dom :as dom]))


(defn fetch-csrf-token []
  (ajax/get-json "/csrf" {} 
                 (fn [response]
                   (dom/set-value "__anti-forgery-token" (:token response)))))
