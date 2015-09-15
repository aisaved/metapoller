(ns centipair.registry
  (:require [centipair.core.user.forms :as user-forms]
            [centipair.social.facebook :as facebook]
            [metapoller.poll :as poll]
            ))


(def function-registry {:render-register-form user-forms/render-register-form
                        :render-login-form user-forms/render-login-form
                        :render-forgot-password-form user-forms/render-forgot-password-form
                        :render-reset-password-form user-forms/render-reset-password-form
                        :fb-init facebook/fb-init
                        :render-poll-ui poll/render-poll-ui})



(defn ^:export load-function [name]
  (((keyword name) function-registry)))
