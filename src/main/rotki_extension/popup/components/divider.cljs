(ns rotki-extension.popup.components.divider 
  (:require [rotki-extension.common.utils :as ut]))

(def default-classes ["divider"])

(defn base 
  [{:keys [text] :as opts}]
  [:div {:class (ut/class-wrapper opts identity default-classes)}
   (or text "")])