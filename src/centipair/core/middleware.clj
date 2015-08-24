(ns centipair.core.middleware
  (:require [centipair.core.auth.user.models :as user-models]))



(defn wrap-user-access
  [handler]
  (fn [request]
    (if (user-models/logged-in? request)
      (handler request)
      "Access denied")))
