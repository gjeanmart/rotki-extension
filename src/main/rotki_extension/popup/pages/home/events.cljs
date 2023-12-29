(ns rotki-extension.popup.pages.home.events
  (:require [re-frame.core :as rf]
            [clojure.string :as str]))

(rf/reg-event-fx
 :home/cache-img
 (fn [_ [_ url]]
   (when (str/starts-with? @url "http")
     {:fx [[:chrome-extension/runtime:send-message {:action     :cache-image
                                                    :data       {:url @url}
                                                    :on-success [:home/cache-img:success url]}]]})))

(rf/reg-event-fx
 :home/cache-img:success
 (fn [] {}))

(rf/reg-event-fx
 :home/fetch-img-from-cache
 (fn [_ [_ url]]
   (if (str/starts-with? @url "http")
     {:fx [[:chrome-extension/runtime:send-message {:action     :fetch-image
                                                    :data       {:url @url}
                                                    :on-success [:home/fetch-img-from-cache:success url]
                                                    :on-failure [:home/fetch-img-from-cache:failure url]}]]}
     (do (reset! url "img/not_found.png") ;;[TODO] fx to update ratom
         {}))))

(rf/reg-event-fx
 :home/fetch-img-from-cache:success
 (fn [_ [_ url img]] 
   (reset! url img) ;;[TODO] fx to update ratom
   {}))

(rf/reg-event-fx
 :home/fetch-img-from-cache:failure
 (fn [_ [_ url _error]]
   (reset! url "img/not_found.png") ;;[TODO] fx to update ratom
   {}))