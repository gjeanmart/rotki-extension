(ns rotki-extension.popup.components.input 
  (:require [rotki-extension.common.utils :as ut]
            [rotki-extension.popup.components.typography :as typography]))

(def default-classes ["input" "input-bordered"])

(defn- size->class
  [size]
  (condp = size
    :large  "input-lg"
    :medium "input-md"
    :small  "input-sm"
    :xsmall "input-xs"
    "input-sm"))

(defn- ->class
  [classes opts]
  (cond-> classes
    (:ghost? opts) (conj "input-ghost")
    (:error? opts) (conj "input-error") 
    true           (conj (size->class (:size opts)))))

(defn base
  [{:keys [type label label-right placeholder value disabled? on-change] :as opts}]
  [:div.form-control
   [:div.flex.flex-row.justify-between.items-center
    (when label
      [:label.label
       [typography/base label]])
    (when label-right
      label-right)]
   [:input {:type        (or type "text")
            :placeholder placeholder
            :value       value
            :on-change   on-change
            :disabled    disabled?
            :class       (ut/class-wrapper opts ->class default-classes)}]])

