(ns rotki-extension.common.chrome-extension
  (:require [rotki-extension.common.utils :as ut]
            [promesa.core :as p]
            [re-frame.core :as rf]))

;; ---- RUNTIME ----

(defn on-install
  "When the extension is installed"
  [callback]
  (.. js/chrome
      -runtime
      -onInstalled
      (addListener #(callback (ut/j->c %)))))

(defn on-startup
  "When the browser starts"
  [callback]
  (.. js/chrome
      -runtime
      -onStartup
      (addListener #(callback (ut/j->c %)))))

(defn send-message
  [msg]
  (p/create
   (fn [resolve _reject]
     (.. js/chrome
         -runtime
         (sendMessage (ut/c->j msg)
                      #(resolve (ut/j->c %)))))))

(defn on-message
  [callback]
  (.. js/chrome
      -runtime
      -onMessage
      (addListener (fn [msg sender send-response]
                     (callback (ut/j->c msg)
                               (ut/j->c sender)
                               send-response)
                      ;; return true to indicate that sendResponse 
                      ;; will be called asynchronously
                     true))))

(rf/reg-fx
 :chrome-extension/runtime:send-message
 (fn [{:keys [action data on-success on-failure]
       :or   {on-failure [:track/error]}}]
   (-> (p/chain (send-message {:action action :data data})
                #(if (= :success (-> % :status keyword))
                   (rf/dispatch (conj on-success (:data %)))
                   (rf/dispatch (conj on-failure (:data %)))))
       (p/catch #(rf/dispatch (conj on-failure %))))))

;; ---- STORAGE ----

(defn storage-get
  [& keys]
  (p/chain (.. js/chrome -storage -local (get (ut/c->j (map #(name %) keys))))
           (fn [results]
             (if (= 1 (count keys))
               (-> results ut/j->c (get (-> keys first keyword)))
               (map #(-> results ut/j->c (get (-> % name keyword))) keys)))))

(defn storage-set
  ;;TODO: review this to work with infinite key-values like assoc
  ([k1 v1]
   (.. js/chrome -storage -local (set (ut/c->j {k1 v1}))))
  ([k1 v1 k2 v2]
   (p/all [(storage-set k1 v1)
           (storage-set k2 v2)]))
  ([k1 v1 k2 v2 k3 v3]
   (p/all [(storage-set k1 v1 k2 v2)
           (storage-set k3 v3)])))

(defn storage-clear
  []
  (.. js/chrome -storage -local (clear)))


;; ---- ALARM ----

(defn alarm-create
  [{:keys [alarm-name delay-min period-min]
    :or  {delay-min 1 period-min 0}}]
  (.. js/chrome
      -alarms
      (create (name alarm-name)
              #js {:delayInMinutes  delay-min
                   :periodInMinutes period-min})))

(defn alarm-delete
  [alarm-name]
  (.. js/chrome
      -alarms
      (clear (name alarm-name))))

(defn alarm-on-tick
  [callback]
  (.. js/chrome
      -alarms
      -onAlarm
      (addListener (fn [result]
                     (callback (-> result
                                   ut/j->c
                                   :name
                                   keyword))))))


;; ---- BADGE ----

(defn badge-set
  [txt]
  (.. js/chrome -action (setBadgeText (ut/c->j {:text txt}))))