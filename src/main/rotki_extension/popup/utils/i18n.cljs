(ns rotki-extension.popup.utils.i18n
  (:require [rotki-extension.common.config :as config]
            [taoensso.tempura :as tempura ]))

(def opts {:dict (config/read :i18n :dictionary)})

(def tr (partial tempura/tr opts [:en :fr]))
