(ns centipair.core.components.tabs)


(defn nav-tab
  [tab]
  [:li {:role "presentation" :class (if (:active @tab) "active" "") :key (str (:id @tab) "-list")}
   [:a {:href (str "#" (:url @tab))
       :aria-controls (:id @tab)
       :role "tab"
       :data-toggle "tab" :key (str (:id @tab) "-link")} (:label @tab)]])


(defn tab-panel
  [tab]
  [:div {:role "tabpanel" :class (str "tab-pane" (if (:active @tab) " active" "")) :id (:id @tab) :key (str (:id @tab) "-panel")}
   ((:content @tab))])



(defn render-tabs [tab-data]
  [:div {:role "tab-panel" :key "tab-panel-div"}
   [:ul {:class "nav nav-tabs"
         :role "tablist"
         :key "tab-list"}
    (doall (map nav-tab tab-data))
    
    ]
   [:div {:class "tab-content" :key "tab-content"}
    (doall (map tab-panel tab-data))
    ]])
