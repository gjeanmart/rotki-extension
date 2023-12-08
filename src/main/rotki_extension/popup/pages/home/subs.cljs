(ns rotki-extension.popup.pages.home.subs
  (:require [re-frame.core :as rf]
            [rotki-extension.common.utils :as ut]))

;; --------- UTILS ---------

(defn- format-label
  [{:keys [amount symbol]}]
  (cond-> ""
    amount (str (ut/format-number amount {:decimals 2}))
    true   (str " " symbol)))


;; --------- SUBS ---------

(rf/reg-sub
 :home/total-balance
 (fn [db _]
   (:rotki/total-balance db)))

(rf/reg-sub
 :home/assets
 (fn [db _] 
   (->> db
        :rotki/assets
        vals
        (remove #(and (-> db :root/settings :hide-zero-balances)
                      (> 0.009 (js/parseFloat (:usd_value %)))))
        (sort-by #(js/parseFloat (:usd_value %)) >))))

(rf/reg-sub
 :home/assets-for-charts
 :<- [:home/total-balance]
 :<- [:home/assets]
 (fn [[_total-balance assets]]
   (let [max-assets               8
         remaining-assets         (drop (- max-assets 1) assets)
         remaining-assets-balance {:usd_value (reduce #(+ %1 (js/parseFloat %2))
                                                      0
                                                      (map :usd_value remaining-assets)),
                                   :name      "OTHERS",
                                   :symbol    "OTHERS"
                                   :color     "#e45426"}
         new-assets               (conj (vec (take (- max-assets 1) assets)) remaining-assets-balance)]
     {:data   (map :usd_value new-assets)
      :labels (map format-label new-assets)
      :colors ["#833ab4" "#b62e75" "#db2547" "#fd1d1d" "#fd4f2b" "#fd7234" "#fc923d" "#fcb045"]})))
