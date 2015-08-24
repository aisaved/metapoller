(ns centipair.core.contrib.cryptography
  (:require [crypto.random :as random]
            [crypto.password.scrypt :as hashers]
            [clj-uuid :as clj-uuid]))


(defn encrypt-password
  "Encrypts password
  Default algorithm bcrypt+sha512"
  [plain-password]
  (hashers/encrypt plain-password))


(defn check-password
  "Compare encrypted password"
  [plain-password encrypted-password]
  (hashers/check plain-password encrypted-password))


(defn random-base64
  "Generate cryptographically secure string"
  [length]
  (random/base64 length))


(defn uuid
  "Clojure UUID supposed to be faster than Java"
  [] 
  (clj-uuid/v1))

(defn java-uuid
  "Uses java's uuid"
  []
  (java.util.UUID/randomUUID))

(defn str-uuid
  "converts string to uuid format"
  [id]
  (java.util.UUID/fromString id))


(defn generate-session-id []
  (random-base64 32))
