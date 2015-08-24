(ns centipair.core.components.editor
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [markdown.core :refer [md->html]]
            [cljs.core.async :refer [put! chan <!]]
            [centipair.core.style :as style]
            [centipair.core.utilities.dom :as dom]))



(def markdown-html-channel (chan))



(def html-tags [[#"&" "&amp;"]  [#"<" "&lt;"] [#">" "&gt;"]])


(defn clean-html-tags [text rule]
  (clojure.string.replace text (first rule) (second rule)))


(defn sanitize [text]
  (.log js.console text)
  (if (= text nil)
    ""
    (reduce clean-html-tags text html-tags)))

(defn markdown-html-preview
  [field]
  [:div (:html field)])



(defn update-markdown-channel [field]
  (put! markdown-html-channel 
        [(str "markdown-preview-" (:id @field))
         (:value @field)]))


(defn update-markdown-value
  [field value]
    (reset! field (assoc @field :value value)))


(defn apply-preview [field]
  (dom/innerHtml (str "markdown-preview-" (:id @field)) (:html @field)))



(defn generate-preview [id-value-group]
  (let [id (first id-value-group)
        md-text (second id-value-group)
        html (md->html (sanitize md-text))]
    (dom/innerHtml id html)))




(defn markdown-editor
  [field]
  [:div {:class (if (nil? (:class-name @field)) style/bootstrap-input-container-class (:class-name @field)) :key (str "container-" (:id @field))}
   [:label {:for (:id @field) :class "col-sm-2 control-label" :key (str "label-" (:id @field))} (:label @field)]
   [:div {:class "col-sm-6" :key (str "divider-" (:id @field))}
    [:div {:key (str "markdown-editor-container" (:id @field))}
     [:ul {:class "nav nav-tabs" :key (str "markdown-editor-menu-container-" (:id @field))}
      [:li {:class "active" :role "presentation" :key (str "markdown-editor-preview-button-" (:id @field))}
       [:a {:href "#md-editor" 
            :aria-controls "md-editor"
            :role "tab"
            :data-toggle "tab"} "Editor"]]
      [:li {:class "" :role "presentation"}
       [:a {:href "#md-preview" 
            :aria-controls "md-preview"
            :role "tab"
            :data-toggle "tab"
            :on-click #(update-markdown-channel field)}
        "Preview"]]]
     [:div {:class "tab-content"}
      [:div {:class "tab-pane active" :role "tab-panel" :id "md-editor"}
       [:div {:class (if (nil? (:class-name @field)) style/bootstrap-input-container-class (:class-name @field))
              :key (str "container-" (:id @field))}
        [:label {:for (:id @field) :class "control-label" :key (str "description-label-" (:id @field))} (:description @field)]
        [:textarea {:class "form-control"
                    :type (:type @field) :id (:id @field)
                    :placeholder
                    (if (nil? (:placeholder @field))
                      ""
                      (:placeholder @field))
                    :rows "10"
                    :value (:value @field)
                    :on-change #(update-markdown-value field (-> % .-target .-value) )
                    :key (:id @field)
                    }]
        [:label {:class "message-label" 
                 :key (str "message-" (:id @field))} (if (nil? (:message @field))
                 ""
                 (:message @field))]]]
      [:div {:class "tab-pane" :role "tab-panel" :id "md-preview"}
       [:div {:class "panel panel-default"}
        [:div {:id (str "markdown-preview-" (:id @field))  :class "panel-body"}]]]]]]])



(defn init-markdown-channel []
  (go (while true
         (generate-preview (<! markdown-html-channel)))))

