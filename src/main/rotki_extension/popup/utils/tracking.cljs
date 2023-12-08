(ns rotki-extension.popup.utils.tracking
  (:refer-clojure :exclude [print])
  (:require [re-frame.core :as rf]))

(defn print 
  [msg]
  (if (object? msg)
    (js/console.log msg)
    (prn msg)))

;; ---- TRACK ----

(rf/reg-fx
 :track/info
 (fn [msg]
   (print msg)))

(rf/reg-event-fx
 :track/info
 (fn [_ [_ error]]
   {:fx [[:track/info error]]}))

(rf/reg-fx
 :track/error
 (fn [error]
   (print error)))

(rf/reg-event-fx
 :track/error
 (fn [_ [_ error]]
   {:fx [[:track/error error]]}))
