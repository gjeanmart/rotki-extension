(ns rotki-extension.common.date
  (:require ["date-fns" :as date-fns]))

(def default-format "yyyy-MM-dd HH:mm")

(defn now
  []
  (-> (js/Date.)
      date-fns/getUnixTime))

(defn format
  [date & [format]]
  (-> date 
      date-fns/fromUnixTime
      (date-fns/format (or format default-format))))
  