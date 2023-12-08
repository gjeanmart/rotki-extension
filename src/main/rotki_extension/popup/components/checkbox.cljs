(ns rotki-extension.popup.components.checkbox
  (:require [rotki-extension.common.utils :as ut]
            [rotki-extension.popup.components.typography :as typography]))

(def default-classes ["checkbox toggle"])

(defn- type->class
  [size]
  (condp = size
    :toggle   "toggle"
    :checkbox "checkbox"
    "checkbox"))

(defn- ->class
  [classes opts]
  (cond-> classes
    (:type opts)   (conj (type->class (:type opts)))))

(defn base
  [{:keys [label value on-change icon-off icon-on checked? disabled?] :as opts}]
  [:div.form-control.flex.flex-row.align-center.justify-between
   (when label
     [:label.label
      [typography/base label]]) 
   [:div.flex.flex-row.align-center.items-center.cursor-pointer.gap-2
    (when icon-off icon-off)
    [:input {:type      "checkbox"
             :value     value
             :checked   checked?
             :disabled  disabled?
             :on-change on-change
             :class     (ut/class-wrapper opts ->class default-classes)}]
    (when icon-on icon-on)]])

