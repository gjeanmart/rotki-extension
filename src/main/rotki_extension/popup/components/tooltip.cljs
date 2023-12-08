(ns rotki-extension.popup.components.tooltip 
  (:require [rotki-extension.common.utils :as ut]))

(def default-classes ["tooltip"])

(defn- position->class
  [size]
  (condp = size
    :top    "tooltip-top"
    :right  "tooltip-right"
    :bottom "tooltip-bottom"
    :left   "tooltip-left"
    "tooltip-right"))

(defn- ->class
  [classes opts]
  (cond-> classes
    true   (conj (position->class (:position opts)))))


(defn base
  [{:keys [content] :as opts} children]
  [:div
   {:data-tip content
    :class    (ut/class-wrapper opts ->class default-classes)}
   children])