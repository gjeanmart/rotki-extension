(ns rotki-extension.popup.components.avatar
  (:require [reagent.core :as r]
            [rotki-extension.common.utils :as ut]))

(def default-classes [])

(defn- ->class
  [classes opts]
  (cond-> classes
    (:rounded? opts) (conj "rounded-full")
    (:size opts)     (conj (str "w-" (:size opts)))))

(defn base
  [{:keys [src placeholder on-load on-error] :as opts} & children]
  (r/with-let [image (r/atom src)]
    [:div.avatar {:class (if placeholder "placeholder" "")}
     [:div {:class (ut/class-wrapper opts ->class default-classes)}
      (if src
        [:img {:src      @image
               :on-error (fn [^js elm]
                           (when on-error
                             (on-error image)
                             (set! (.. elm -currentTarget -onerror) nil)))
               :on-load  (fn [^js elm]
                           (when on-load
                             (on-load image)
                             (set! (.. elm -currentTarget -onload) nil)))}]
        [:div.text-xl (or placeholder "")])
      children]
    
       ;; trick to force tailwind-css to load classes w-<size>
     [:div {:style {:display "none" :class "w-2 w-4 w-6 w-8 w-10 w-12 w-14 w-16"}}]]))

  
