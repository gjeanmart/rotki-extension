(ns rotki-extension.common.date
  (:require ["date-fns" :as date-fns]
            [rotki-extension.common.utils :as ut]))

(def default-format "yyyy-MM-dd HH:mm")

(defn now
  "Return current date in unix timestamp (epoch)"
  []
  (-> (js/Date.)
      date-fns/getUnixTime))

(defn move
  "Move date by duration {:years 1 :months 1 :days 1 :hours 1 :minutes 1 :seconds 1}
   Use positive number to add and negative numbers to subtract"
  [date duration]
  (-> date
      date-fns/fromUnixTime
      (date-fns/add (ut/c->j duration))
      date-fns/getUnixTime))

(defn format
  "Format date to string
   See https://date-fns.org/v2.21.1/docs/format for more details"
  [date & [format]]
  (-> date
      date-fns/fromUnixTime
      (date-fns/format (or format default-format))))
  