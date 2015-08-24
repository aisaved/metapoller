(ns centipair.core.config
  (:require [clojure.edn :as edn]))


(defn get-config
  []
  (clojure.edn/read-string (slurp "config.edn")))


(defn load-sql-db-config
  "Loads db config from congif.edn file in classpath
  keys: :db-name :db-username :db-password
  "
  []
  (let [config-data (get-config)]
    (get-in config-data [:db :sql])))


;;sql db config
(defonce sql-db-config (load-sql-db-config))


(defn load-cassandra-config
  []
  (let [config-data (get-config)]
    (get-in config-data [:db :cassandra])))


(defonce cassandra-config (load-cassandra-config))
