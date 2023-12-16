(ns rotki-extension.popup.pages.settings.view
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [rotki-extension.common.config :as config]
            [rotki-extension.popup.components.button :as button]
            [rotki-extension.popup.components.checkbox :as checkbox]
            [rotki-extension.popup.components.icons :as icons]
            [rotki-extension.popup.components.input :as input]
            [rotki-extension.popup.components.tooltip :as tooltip]
            [rotki-extension.popup.pages.settings.events]
            [rotki-extension.popup.pages.settings.subs]
            [rotki-extension.popup.utils.i18n :refer [tr]]))

(defn- tooltip 
  [txt]
  [tooltip/base {:content  txt
                 :position :left}
   [icons/info {:size  20 :color :grey}]])

(defn- content
  [initial-data]
  (r/with-let [data (r/atom initial-data)
               update-field #(swap! data assoc %1 %2 :updated? true)
               save-settings #(do (update-field :loading? true)
                                  (rf/dispatch [:settings/save @data]))]
    [:div.flex.flex-col.gap-2.m-2
     [input/base {:label       (tr [:setting/form:endpoint])
                  :label-right [tooltip (tr [:setting/form:endpoint:tooltip])]
                  :value       (:rotki-endpoint @data)
                  :on-change   #(update-field :rotki-endpoint (-> % .-target .-value))}]
     
     [input/base {:label       (tr [:setting/form:timeout])
                  :label-right [tooltip (tr [:setting/form:timeout:tooltip])] 
                  :type        :number
                  :value       (:rotki-timeout-sec @data)
                  :on-change   #(update-field :rotki-timeout-sec (-> % .-target .-value))}]
     
     [input/base {:label       (tr [:setting/form:snapshot-ttl])
                  :label-right [tooltip (tr [:setting/form:snapshot-ttl:tooltip])]
                  :type        :number
                  :value       (:rotki-snapshot-ttl-min @data)
                  :on-change   #(update-field :rotki-snapshot-ttl-min (-> % .-target .-value))}]
     
     [input/base {:label       (tr [:setting/form:background-refresh])
                  :label-right [tooltip (tr [:setting/form:background-refresh:tooltip])]
                  :type        :number
                  :value       (:rotki-refresh-data-min @data)
                  :on-change   #(update-field :rotki-refresh-data-min (-> % .-target .-value))}]
     
     [checkbox/base {:label     (tr [:setting/form:hide-zero-balances])
                     :value     (:hide-zero-balances @data)
                     :checked?  (:hide-zero-balances @data)
                     :type      :checkbox
                     :on-change #(update-field :hide-zero-balances (not (:hide-zero-balances @data)))}]

     (when (config/dev?)
       [checkbox/base {:label     (tr [:setting/form:use-mocked-data?])
                       :value     (:use-mocked-data? @data)
                       :checked?  (:use-mocked-data? @data)
                       :type      :checkbox
                       :on-change #(update-field :use-mocked-data? (not (:use-mocked-data? @data)))}])
     ;; Hide theme for now
    ;;  [checkbox/base {:label     (tr [:setting/form:theme])
    ;;                  :value     (:theme @data)
    ;;                  :checked?  (= (:theme @data) "dark")
    ;;                  :type      :toggle
    ;;                  :icon-off  [icons/sun {:size 20}]
    ;;                  :icon-on   [icons/moon {:size 20}]
    ;;                  :on-change #(update-field :theme (if (= (:theme @data) "light") "dark" "light"))}]
     
     [:div.flex.justify-center
      [button/base {:on-click  save-settings
                    :size      :sm
                    :block?     true
                    :icon-left [icons/floppy]
                    :loading   (:loading? data)}
       (tr [:setting/form:save])]]]))

(defn page
  []
  (let [settings    @(rf/subscribe [:profile/settings])]
    [content (merge settings {:loading? false
                              :updated? false})]))