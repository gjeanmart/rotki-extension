(ns rotki-extension.popup.components.loader 
  (:require [rotki-extension.common.utils :as ut]))

(def default-classes ["loading"])

(defn- size->class
  [size]
  (condp = size
    :large  "loading-lg"
    :medium "loading-md"
    :small  "loading-sm"
    :xsmall "loading-xs"
    "loading-md"))

(defn- type->class
  [type]
  (condp = type
    :spinner "loading-spinner"
    :ring    "loading-ring"
    "loading-spinner"))

(defn- ->class
  [classes opts]
  (cond-> classes
    (:type opts)      (conj (type->class (:type opts)))
    (:size opts)      (conj (size->class (:size opts)))))

(defn base
  [opts]
  [:div {:class (ut/class-wrapper opts ->class default-classes)}])

(defn ring 
  [& [opts]]
  [base (assoc opts :type :ring)])

(defn spinner
  [& [opts]]
  [base (assoc opts :type :spinner)])