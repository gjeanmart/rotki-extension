(ns rotki-extension.sw.log
  (:require [clojure.string :refer [upper-case join]]
            [rotki-extension.common.date :as date]))

(defn- format-log 
  [level messages]
  (let [date-formated  (-> (date/now) (date/format "yyyy-MM-dd HH:mm:ss"))
        level-formated (-> level name upper-case)
        message        (->> messages
                            (map #(cond-> %
                                    (object? %) (js/JSON.stringify)
                                    true         (str)))
                            (join " "))]
    (str "[" date-formated " "  level-formated "] " message)))

(defn- base
  [level messages]
  (cond->> messages
    true               (format-log level)
    (= :error level)   (js/console.error)
    (= :warning level) (js/console.warn)
    (= :info level)    (js/console.info)
    (= :debug level)   (js/console.debug)))

(defn log
  [level & messages]
  (base level messages))

(defn debug 
  [& messages]
  (base :debug messages))

(defn info
  [& messages]
  (base :info messages))

(defn warning
  [& messages]
  (base :warning messages))

(defn error
  [& messages]
  (base :error messages))
