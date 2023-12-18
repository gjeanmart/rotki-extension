(ns rotki-extension.popup.components.dropdown
  (:require [reagent.core :as r]
            [rotki-extension.common.utils :as ut]))

(def default-classes [])

(defn base 
  [{:keys [options tabindex]} & [children]]
  (r/with-let [opened? (r/atom false)]
    [:div.dropdown.dropdown-bottom.dropdown-end
     {:class (ut/classes->string [(when @opened? "dropdown-open")]) }
     [:label.btn.btn-circle {:tabIndex 0
                             :role     "button"
                             :on-click #(reset! opened? (not @opened?))}
      children]
     [:ul.dropdown-content.menu.shadow.bg-base-100.rounded-box.border-2.divide-y.divide-slate-200
      {:class (ut/classes->string ["z-[1]"
                                   (when-not @opened? "hidden")])
       :tabIndex tabindex}
      (for [option-id (range (count options))
            :let [option (get options option-id)]]
        ^{:key (str "dropdown-" option-id)}
        [:li {:on-click #(reset! opened? false)}
         option])]]))