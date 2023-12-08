(ns rotki-extension.popup.components.button
  (:require [rotki-extension.common.utils :as ut]
            [rotki-extension.popup.components.loader :as loader]
            [rotki-extension.popup.components.typography :as typography]))

(def default-classes ["btn"])

(defn- theme->class 
  [theme]
  (condp = theme
    :primary   "btn-primary"
    :secondary "btn-secondary"
    :neutral   "btn-neutral"
    ""))

(defn- size->class
  [size]
  (condp = size
    :la "btn-lg"
    :sm "btn-sm"
    :xs "btn-xs"
    "btn-sm"))

(defn ->class 
  [classes opts]
  (cond-> classes
    (:disabled? opts) (conj "btn-disabled")
    (:loading? opts)  (conj "btn-disabled")
    (:outline? opts)  (conj "btn-outline")
    (:block? opts)    (conj "btn-block")
    true              (conj (theme->class (:theme opts)))
    true              (conj (size->class (:size opts)))))


(defn base
  [{:keys [on-click loading? icon-left icon-right] :as opts} & children]
  [:div {:class    (ut/class-wrapper opts ->class default-classes)
         :on-click on-click}
   [:<>
    (when (and icon-left (not loading?))  
      [:div icon-left])
    (when loading?
      [loader/spinner])
    [typography/base {:weight :bold}
     children]
    (when icon-right
      [:div icon-right])]])

(defn primary
  [opts children]
  [base (assoc opts :theme :primary)
   children])

(defn secondary
  [opts children]
  [base (assoc opts :theme :secondary)
   children])

(defn neutral
  [opts children]
  [base (assoc opts :theme :neutral)
   children])