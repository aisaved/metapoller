(ns centipair.core.errors)


;;Example :email "Not valid email"

(defn validation-error [key message]
  [false {:validation-result {:errors {key #{message}}}}])
