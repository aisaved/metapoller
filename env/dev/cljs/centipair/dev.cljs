(ns ^:figwheel-no-load centipair.app
  (:require [centipair.init :as init]
            [figwheel.client :as figwheel :include-macros true]
            [centipair.registry :as function-registry]
            [weasel.repl :as weasel]
            [reagent.core :as r]))

(enable-console-print!)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  ;;:jsload-callback core/mount-components
  )

(weasel/connect "ws://localhost:9001" :verbose true)

(init/init!)
