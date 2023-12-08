(ns rotki-extension.popup.components.alert 
  (:require [rotki-extension.common.utils :as ut]))

(def default-classes ["alert m-4"])

(defn- type->class
  [type]
  (condp = type
    :info    "alert-info"
    :warning "alert-warning"
    :error   "alert-error"
    :success "alert-success"
    ""))

(defn- ->class
  [classes opts]
  (cond-> classes
    (:type opts) (conj (type->class (:type opts)))
    true         (ut/classes->string)))

(defn base
  [{:keys [icon hide?] :as opts} & children]
  (when-not hide?
    [:div {:class (ut/class-wrapper opts ->class default-classes)}
     [:<>
      (when icon  [:div icon])
      children]]))

(defn info
  [opts children]
  [base (assoc opts :type :info)
   children])

(defn success
  [opts children]
  [base (assoc opts :type :success)
   children])

(defn warning
  [opts children]
  [base (assoc opts :type :warning)
   children])

(defn error
  [opts children]
  [base (assoc opts :type :error)
   children])