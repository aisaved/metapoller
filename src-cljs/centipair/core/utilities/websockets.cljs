(ns centipair.core.utilities.websockets
  (:require [cljs.core.async :refer [<! >! put! close! chan]]
            [goog.dom :as gdom]
            [reagent.core :as reagent :refer [atom]])
  (:require-macros [cljs.core.async.macros :refer [go]]))



(def websockets-port 8081)

(defn websockets-host []
  (let [host (.-host (.-location js/window))
        domain (first (clojure.string/split host #":"))] 
    (str domain ":" websockets-port)))

(defn get-session-token []
  (let [session-token (.getAttribute (gdom/getElement "session_token") "value")]
    session-token
    ))

(defn websockets-url [end-point]
  (str "ws://" (websockets-host) end-point "?session_token=" (get-session-token)))


(defn make-sender [send-channel websocket]
  (go 
    (while true
      (let [message (<! send-channel)]
      (.send websocket  (.stringify js/JSON (clj->js message)))))))


(defn websockets-connect [end-point]
  (let [websocket (new js/WebSocket (websockets-url end-point))
        send-channel (chan)
        receive-channel (chan)
        ]
    (do 
      (set! (.-onmessage websocket) (fn [msg] (put! receive-channel msg)))
      (make-sender send-channel websocket))
    {:send-channel send-channel
     :receive-channel receive-channel}
    ))


(defn pure-websocket [end-point callback]
  (let [url (websockets-url end-point)
        websocket (new js/WebSocket (websockets-url end-point))]
    (set! (.-onmessage websocket) callback)
    websocket))
