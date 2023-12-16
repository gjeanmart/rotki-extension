(ns rotki-extension.sw.core
  (:require [promesa.core :as p]
            [rotki-extension.common.chrome-extension :as chrome-extension]
            [rotki-extension.common.config :as config]
            [rotki-extension.common.http :as http]
            [rotki-extension.common.utils :as ut]
            [rotki-extension.sw.cache :as cache]
            [rotki-extension.sw.log :as log]
            [rotki-extension.sw.rotki :as rotki]))

(defn on-install
  [{:keys [reason]}]
  (when (= reason "install") 
    (let [default-settings (config/read :default-settings)]
      (log/info "Installing extension with default-settings::" default-settings)
      (p/chain (chrome-extension/storage-clear)
               #(chrome-extension/storage-set :settings default-settings)))))

(defn on-message-received
  [{:keys [action data]} _sender send-response]
  (log/debug "on-message-received : action=" action ", data=" data)
  (let [success (fn [& [result]]
                  (send-response (ut/c->j {:action action
                                           :status :success
                                           :data   result})))
        failure  (fn [error & [{:keys [level] :or {level :error}}]]
                   (log/log level "on-message-received : action=" action ", data=" data ", error=" error)
                   (send-response (ut/c->j {:action action
                                            :status :failure
                                            :data   error})))]
    (condp = (keyword action)

      ;; ------ SETTINGS ------
      :set-settings
      (-> (p/chain (chrome-extension/storage-set :settings data)
                   #(cache/remove rotki/cache-key)
                   #(success data))
          (p/catch #(failure %)))

      :get-settings
      (-> (p/chain (chrome-extension/storage-get :settings)
                   #(success %))
          (p/catch #(failure %)))

      ;; ------ IMAGE (CACHING) ------
      :cache-image
      (p/let [cache-key   (-> data :url ut/sha256 keyword)
              cache-value (cache/read cache-key)]
        (if-not cache-value
          ;; Not cached
          (-> (p/let [base64 (http/get {:url (:url data) :output :blob :timeout 5000})
                      _      (cache/write cache-key base64 {:ttl 31536000})] ;; 1y ttl
                (success true))
              (p/catch #(failure %)))
          ;; already cached
          (success true)))

      :fetch-image
      (-> (p/let [cache-key      (-> data :url ut/sha256 keyword)
                  {:keys [data]} (cache/read cache-key)]
            (if data
              (success data)
              (failure "Image not found in cache" {:level :warning})))
          (p/catch #(failure %)))

      ;; ------ ROTKI ------
      :get-rotki-data
      (p/chain  (chrome-extension/storage-get :settings)
                #(rotki/fetch-data {:settings %
                                    :success  success
                                    :failure  failure}))

      ;; ------ DEFAULT ------
      (failure (str "No handler for action " action)))))

(defn on-alarm-tick
  [alarm-name]
  (log/info "on-alarm-tick : alarm-name=" alarm-name)
  (condp = alarm-name
    :get-rotki-data
    (p/chain (chrome-extension/storage-get :settings)
             #(rotki/fetch-data {:settings       %
                                 :force-refresh? true
                                 :success        identity
                                 :failure        (fn [err] (log/warning "Error fetching data" err))}))

      ;; ------ DEFAULT ------
    (log/warning (str "No handler found for alarm " alarm-name))))

(defn init []
  (p/let [_ (chrome-extension/on-install    #(on-install %1))
          _ (chrome-extension/on-message    #(on-message-received %1 %2 %3))
          _ (chrome-extension/alarm-on-tick #(on-alarm-tick %1))
          _ (chrome-extension/alarm-create  {:alarm-name :get-rotki-data
                                             :delay-min  1
                                             :period-min (config/read :default-settings :rotki-refresh-data-min)})]))

