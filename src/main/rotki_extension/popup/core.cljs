(ns rotki-extension.popup.core
  (:require [promesa.core :as p]
            [re-frame.core :as rf]
            [reagent.dom.client :as rdom]
            [rotki-extension.common.chrome-extension :as chrome-extension]
            [rotki-extension.common.config :as config]
            [rotki-extension.popup.layout :as layout]
            [rotki-extension.popup.utils.tracking]))

(defonce root (rdom/create-root (js/document.getElementById "root")))

(defn render []
  (rdom/render root [layout/base]))

(defn init
  []
  (rf/dispatch [:core/boot])
  (render))

(rf/reg-event-fx
 :core/boot
 (fn []
   {:db (config/read :init-db)
    :fx [[:chrome-extension/runtime:send-message {:action     :get-settings
                                                  :on-success [:db/set :root/settings]}]
         [:chrome-extension/runtime:send-message {:action     :get-rotki-data
                                                  :on-success [:core/boot:success]}]]}))

(rf/reg-event-fx
 :core/boot:success
 (fn [{db :db} [_ {:keys [connected data snapshot-at]}]]
   (let [{:keys [total-balance assets]} data]
     {:db (assoc db
                 :root/loading?       false
                 :rotki/connected?    connected
                 :rotki/snapshot-at   snapshot-at
                 :rotki/total-balance total-balance
                 :rotki/assets        assets)})))

(rf/reg-sub
 :root/loading?
 (fn [db _]
   (:root/loading? db)))

(rf/reg-sub
 :rotki/connected?
 (fn [db _]
   (:rotki/connected? db)))

(rf/reg-sub
 :rotki/snapshot-at
 (fn [db _]
   (:rotki/snapshot-at db)))

;; ------ NAVIGATION ------
;; TODO move to utils/router.cljs

(rf/reg-event-fx
 :root/navigate
 (fn [{:keys [db]}  [_ page]]
   {:db (assoc db :root/page page)}))

(rf/reg-sub
 :root/page
 (fn [db _]
   (:root/page db)))

;; ------ UTILS ------
;; TODO move to utils/reframe.cljs

(rf/reg-event-db
 :db/set
 (fn [db [_ & args]]
   (assoc-in db (butlast args) (last args))))


(rf/reg-fx
 :chrome-extension/runtime:send-message
 (fn [{:keys [action data on-success on-failure]
       :or   {on-failure [:track/error]}}]
   (-> (p/chain (chrome-extension/send-message {:action action :data data})
                #(if (= :success (-> % :status keyword))
                   (rf/dispatch (conj on-success (:data %)))
                   (rf/dispatch (conj on-failure (:data %)))))
       (p/catch #(rf/dispatch (conj on-failure %))))))
