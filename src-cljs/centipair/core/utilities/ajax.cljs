(ns centipair.core.utilities.ajax
  (:require [ajax.core :refer [GET POST DELETE json-request-format edn-request-format json-response-format]]
            [centipair.core.utilities.dom :as dom])
  (:use [centipair.core.components.notifier :only [notify]]
        [centipair.core.components.input :only [append-error]]))


(defn error-handler [response]
  (.log js/console "error occured")
  (let [status (:status response)]
    (case status
      401 (notify 401)
      403 (notify 403)
      404 (notify 404)
      405 (notify 405)
      500 (notify 500)
      422 (notify 422 "The submitted data is not valid"))))

(defn success-handler [function-handler response]
  (notify 200)
  (function-handler response))

(defn post [url params function-handler]
  (do
    (notify 102 "Loading")
    (POST url
          :params params
          :handler (partial success-handler function-handler)
          :error-handler error-handler
          :format (json-request-format)
          :headers {:X-CSRF-Token (dom/get-value "__anti-forgery-token")}
          :response-format (json-response-format {:keywords? true}))))


(defn post-file [url params function-handler]
  (.log js/console params)
  (do
    (notify 102 "Loading")
    (POST url
          :params params
          :handler (partial success-handler function-handler)
          :error-handler error-handler
          ;;:format (json-request-format)
          :headers {:X-CSRF-Token (dom/get-value "__anti-forgery-token")}
          :response-format (json-response-format {:keywords? true}))))



(defn get-json [url params function-handler]
  (do
    (notify 102 "Loading")
    (GET url
         :params params
         :handler (partial success-handler function-handler)
         :error-handler error-handler
         :format (json-request-format)
         :response-format (json-response-format {:keywords? true}))))

(defn delete [url function-handler]
  (notify 102 "Deleting")
  (DELETE url
          :handler (partial success-handler function-handler)
          :headers {:X-CSRF-Token (dom/get-value "__anti-forgery-token")}
          :error-handler error-handler))

(defn set-value-type [field]
  (case (:value-type @field)
    nil (:value @field)
    "string" (:value @field)
    "integer" (js/parseInt  (:value @field))
    "float" (js/parseFloat  (:value @field))
    (:value @field)))


(defn set-select-value-type [field]
  (let [fetched-value (dom/get-value (:id @field))]
    (case (:value-type @field)
    nil fetched-value
    "string" fetched-value
    "integer" (js/parseInt  fetched-value)
    "float" (js/parseFloat  fetched-value)
    fetched-value)
    ))

(defn text-to-key [previous each]
  (assoc previous (keyword (:id @each)) (set-value-type each)))

(defn check-to-key[previous each]
  (if (= (:checked @each) "checked")
      (assoc previous (keyword (:id @each)) true)
      (assoc previous (keyword (:id @each)) false)))

(defn image-spec-to-key[previous each]
  (assoc previous 
    (keyword (:id (:width @each))) (js/parseInt (:value (:width @each)))
    (keyword (:id (:height @each))) (js/parseInt (:value (:height @each)))
    (keyword (:id (:crop @each))) (= (:checked (:crop @each)) "checked")))

(defn select-to-key [previous each]
  (assoc previous (keyword (:id @each)) (set-select-value-type each)))

(defn select-text-to-key
  [previous each]
  (assoc previous 
    (keyword (:id (:text @each))) (:value (:text @each))
    (keyword (:id (:select @each))) (dom/get-value (:id (:select @each)))))

(defn to-key
  "each is an atom"
  [previous each]
  (case (:type @each)
    "text" (text-to-key previous each)
    "checkbox" (check-to-key previous each)
    "image-spec" (image-spec-to-key previous each)
    "hidden" (text-to-key previous each)
    "select" (select-to-key previous each)
    "select-text" (select-text-to-key previous each)
    (assoc previous (keyword (:id @each)) (set-value-type each))))


(defn handle-form-error [form-state form response]
  (notify 422 "The submitted data is not valid")
  (let [combined (conj form form-state)]
    (doseq [each combined]
      (append-error (:errors (:response response)) each))))

(defn form-error-handler [form-state form response]
  (let [status (:status response)]
    (case status
      401 (notify 401)
      403 (notify 403)
      404 (notify 404)
      405 (notify 405)
      500 (notify 500)
      422 (handle-form-error form-state form response))))


(defn form-success-handler []
  ;;TODO: why was this created?
  )


(defn form-post [form-state url form function-handler]
  (do
    (notify 102 "Loading")
    (POST url
          :params (reduce to-key {} form)
          :handler (partial success-handler function-handler)
          :error-handler (partial form-error-handler form-state form)
          ;;:format (json-request-format)
          :headers {:X-CSRF-Token (dom/get-value "__anti-forgery-token")}
          :response-format (json-response-format {:keywords? true}))))

(defn data-saved-notifier [response]
  (notify 102 "Data saved"))
