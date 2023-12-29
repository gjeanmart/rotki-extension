(ns rotki-extension.popup.pages.settings.events
  (:require [re-frame.core :as rf]))

(rf/reg-event-fx
 :settings/save
 (fn [_ [_ data]]
   {:fx [[:chrome-extension/runtime:send-message {:action     :set-settings
                                                  :data       (dissoc data :loading? :updated?)
                                                  :on-success [:core/boot {:force-refresh? true}]}]]}))