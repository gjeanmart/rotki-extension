(ns rotki-extension.popup.pages.settings.subs
  (:require [re-frame.core :as rf]
            [rotki-extension.common.config :as config]))

(rf/reg-sub
 :profile/settings
 (fn [db]
   (:root/settings db)))

(rf/reg-sub
 :profile/theme
 (fn [db]
   (let [theme (-> db
                   :root/settings
                   :theme
                   keyword)]
     (config/read :theme theme))))