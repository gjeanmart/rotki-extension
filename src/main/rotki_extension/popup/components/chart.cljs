(ns rotki-extension.popup.components.chart
  (:require ["chart.js" :refer [ArcElement Chart Tooltip]]
            ["react-chartjs-2" :refer [Doughnut]]
            [rotki-extension.common.utils :as ut]))

(def background-colors ["#C1C3DA" "#8A8CAC" "#616384" "#3F4053" "#2E303F"])

(defn- doughnut-chart
  [{:keys [data options plugins]}]
  (.. Chart (register ArcElement Tooltip)) 
  [:> Doughnut {:data    (ut/c->j data)
                :options (ut/c->j options)
                :plugins (ut/c->j plugins)}])

(defn doughnut
  [{:keys [class label labels data colors middle-text]}]
  [:div {:class class}
   [doughnut-chart {:data    {:labels   labels
                              :datasets [{:label          label
                                          :data            data
                                          :backgroundColor colors
                                          :borderColor     colors
                                          :borderWidth     1
                                          :borderRadius    5
                                          :hoverOffset     5
                                          :spacing         2}]}
                    :options {:cutout              "80%"
                              :responsive          true
                              :maintainAspectRatio false
                              :layout              {:padding 12}
                              ;; half doughnut
                              :rotation            -90,
                              :circumference       180}
                    :plugins [{:id               "innerLabel",
                               :afterDatasetDraw (fn [^js chart ^js args _pluginOptions]
                                                   (let [x (-> args .-meta .-data (aget 0) .-x)
                                                         y (-> args .-meta .-data (aget 0) .-y)]
                                                     (.. chart -ctx (save))
                                                     (set! (.. chart -ctx -textAlign) "center")
                                                     (set! (.. chart -ctx -font) "32px sans-serif")
                                                     (.. chart -ctx (fillText middle-text x y))
                                                     (.. chart -ctx (restore))))}]}]])