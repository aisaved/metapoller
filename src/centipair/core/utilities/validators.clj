(ns centipair.core.utilities.validators
  "Functions for validating input and setting string errors on fields.
  All fields are simply keys, meaning this can be a general error storage and
  retrieval mechanism for the lifetime of a single request. Errors are not
  persisted and are cleaned out at the end of the request."
  )


;; validation helpers

(defn has-value?
  "Returns true if v is truthy and not an empty string."
  [v]
  (and v (not= v "")))

(defn has-values?
  "Returns true if all members of the collection has-value? This works on maps as well."
  [coll]
  (let [vs (if (map? coll)
             (vals coll)
             coll)]
    (every? has-value? vs)))

(defn not-nil?
  "Returns true if v is not nil"
  [v]
  (boolean (or v (false? v))))

(defn min-length?
  "Returns true if v is greater than or equal to the given len"
  [v len]
  (>= (count v) len))

(defn max-length?
  "Returns true if v is less than or equal to the given len"
  [v len]
  (<= (count v) len))


(defn matches-regex?
  "Returns true if the string matches the given regular expression"
  [v regex]
  (boolean (re-matches regex v)))


(defn is-email?
  "Returns true if v is an email address"
  [v]
  (if (nil? v)
    false
    (matches-regex? v #"(?i)[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")))


(defn valid-file?
  "Returns true if a valid file was supplied"
  [m]
  (and (:size m)
       (> (:size m) 0)
       (:filename m)))


(defn valid-number?
  "Returns true if the string can be parsed to a Long"
  [v]
  (try
    (Long/parseLong v)
    true
    (catch Exception e
      false)))


(defn greater-than?
  "Returns true if the string represents a number > given."
  [v n]
  (and (valid-number? v)
       (> (Long/parseLong v) n)))


(defn less-than?
  "Returns true if the string represents a number < given."
  [v n]
  (and (valid-number? v)
       (< (Long/parseLong v) n)))

(defn equal-to?
  "Returns true if the string represents a number = given."
  [v n]
  (and (valid-number? v)
       (== (Long/parseLong v) n)))


(defn is-username?
  "Returns true if v is a valid username"
  [v]
  (if (nil? v)
    false
    (matches-regex? v #"^(?=.{3,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$")))


(defn is-email-proxy? [v]
  (is-email? v)) 
