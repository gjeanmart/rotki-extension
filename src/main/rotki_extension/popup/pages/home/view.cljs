(ns rotki-extension.popup.pages.home.view
   (:require [re-frame.core :as rf]
             [rotki-extension.common.date :as date]
             [rotki-extension.common.utils :as ut]
             [rotki-extension.popup.components.avatar :as avatar]
             [rotki-extension.popup.components.chart :as chart]
             [rotki-extension.popup.components.icons :as icons]
             [rotki-extension.popup.components.table :as table]
             [rotki-extension.popup.components.typography :as typography]
             [rotki-extension.popup.pages.home.events]
             [rotki-extension.popup.pages.home.subs]
             [rotki-extension.popup.utils.i18n :refer [tr]]))

(defn- no-data
  []
  [:div.flex.flex-col.items-center.justify-center.h-full.w-full.gap-2
   [:div.flex.flex-row.align-center.gap-1
    [icons/warning {:size 20}]
    [typography/base {:center? true
                      :weight  :bold}
     (tr ["No data found"])]]])
 
(defn page
  []
  (let [snapshot-at                  @(rf/subscribe [:rotki/snapshot-at])
        assets                       @(rf/subscribe [:home/assets])
        total-balances               @(rf/subscribe [:home/total-balance])
        {:keys [data labels colors]} @(rf/subscribe [:home/assets-for-charts])]
    [:div.flex.flex-col.items-center.h-full.w-full.gap-2
     (if (seq assets)
       [:<>
        ;; Doughnut chart
        [chart/doughnut {:class       "w-[300px] h-[200px]"
                         :middle-text (ut/format-currency total-balances {:currency :USD})
                         :label       "$"
                         :labels      labels
                         :data        data
                         :colors      colors}]

        ;; Last update
        [:div.flex.justify-end.w-full.pr-2
         [typography/base {:size   :xs
                           :weight :thin
                           :color  :grey}
          (tr [:setting/home:last-update] [(date/format snapshot-at)])]]

        ;; Assets table
        [table/base {:columns [{:key          :icon
                                :label        nil
                                :class        "w-[16px]"
                                :show-tooltip nil}
                               {:key          :name
                                :label        (tr [:setting/home:column:name])
                                :class        "w-[48px] text-left"
                                :show-tooltip #(> (count %) 6)}
                               {:key          :amount
                                :label        (tr [:setting/home:column:amount])
                                :class        "w-[80px] text-left"
                                :show-tooltip nil}
                               {:key          :usd-value
                                :label        (tr [:setting/home:column:usd])
                                :class        "w-[80px] text-left"
                                :show-tooltip nil}]
                     :rows    (doall 
                               (map (fn [{:keys [image_url symbol name amount usd_value]}]
                                      {:icon      [avatar/base {:size        4
                                                                :src         image_url
                                                                :rounded?    true
                                                                :placeholder symbol
                                                                :on-load     #(rf/dispatch [:home/cache-img %])
                                                                :on-error    #(rf/dispatch [:home/fetch-img-from-cache %])}]
                                       :name      (or symbol name)
                                       :amount    (ut/format-number amount {:decimals 2})
                                       :usd-value (ut/format-currency usd_value {:currency :USD})})
                                    assets))}]]
       ;; No data found
       [no-data])]))
