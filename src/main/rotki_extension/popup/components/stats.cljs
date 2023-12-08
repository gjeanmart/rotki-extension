(ns rotki-extension.popup.components.stats 
  (:require [rotki-extension.common.utils :as ut]
            [rotki-extension.popup.components.typography :as typography]))

(defn base
  [{:keys [title value description]}]
  [:div.stats.shadow-md.border.w-full
   [:div.stat
    (when title
      [:div.stat-title
       [typography/base {:weight :bold :size :lg}
        title]])
    [:div.stat-value 
     [typography/base {:weight :semibold :size :2xl}
      value]]
    (when description
      [:div.stat-desc 
       [typography/base {:size :sm :italic? true}
        description]])]])

