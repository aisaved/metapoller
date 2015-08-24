(ns centipair.admin.init
  (:require [centipair.admin.menu :as admin-menu]
            [centipair.core.components.editor :as editor]
            [centipair.core.components.notifier :as notifier]
            ))


(defn init-admin [] 
  (do
    (admin-menu/render-admin-menu)
    (editor/init-markdown-channel)
    (notifier/render-notifier-component)
    ))
