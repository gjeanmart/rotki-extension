(ns rotki-extension.popup.components.table 
  (:require [rotki-extension.popup.components.tooltip :as tooltip]))

(defn base 
  [{:keys [columns rows]}]
  [:div.w-full.overflow-y-auto
   [:table.table.table-xs.table-zebra.table-pin-rows
    [:thead 
     [:tr 
      (for [{:keys [key label class]} columns]
        ^{:key (str "column-" key)}
        [:th 
         [:div.truncate {:class class}
          label]])]]
    [:tbody 
     (for [{:keys [id] :as row} (map-indexed #(assoc %2 :id %1) rows)]
       ^{:key (str "row-" id)}
       [:tr 
        (for [{:keys [key class show-tooltip]} columns
              :let [content [:div.truncate {:class class}
                             (get row key)]]]
          ^{:key (str "row-" key "-" id)}
          [:td
           (if (and show-tooltip (show-tooltip (get row key)))
             [tooltip/base {:content  (get row key) 
                            :position :bottom}
              content]
             content)])])]]])
   
