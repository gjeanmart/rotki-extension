(ns rotki-extension.units-tests.sw.core-test
  (:require [cljs.test :refer-macros [use-fixtures is testing]]
            [date-fns :as date-fns]
            [promesa.core :as p]
            [rotki-extension.common.config :as config]
            [rotki-extension.common.utils :as ut]
            [rotki-extension.sw.core :as core]
            [rotki-extension.sw.rotki :as rotki]
            [rotki-extension.test-helpers :as h]))

(def default-settings (config/read :default-settings))
(def in-memory-storage (atom {}))
(def mock-date (js/Date. "2023-01-01T00:00:00.000"))
(def mock-date-epoch (date-fns/getUnixTime mock-date))
(def stubs [{:url    (:rotki-endpoint default-settings)
             :path   "/api/1/ping"
             :body   (rotki/mock-request "/api/1/ping")}
            {:url    (:rotki-endpoint default-settings)
             :path   "/api/1/balances"
             :query  {:ignore_cache false
                      :save_data    false}
             :body   (rotki/mock-request "/api/1/balances")}
            {:url    (:rotki-endpoint default-settings)
             :path   "/api/1/assets/mappings"
             :method :post
             :body   (rotki/mock-request "/api/1/assets/mappings")}])

(use-fixtures :each
  {:before #(do (h/clean-all-stubs)
                (reset! in-memory-storage {}))})

(defn- promise-send-message
  [action & [data]]
  (p/create (fn [resolve _reject]
              (core/on-message-received {:action (name action)
                                         :data   data}
                                        "test-sender"
                                        (fn [resp] (resolve (ut/j->c resp)))))))

;; ================= TESTS (on-install) =================

(h/deftest-async test-core-on-install
  (testing "test core/on-install"
    (h/with-mock-js-chrome in-memory-storage
      (p/chain (core/on-install {:reason "install"})
               #(is (= (:settings @in-memory-storage) default-settings))))))


;; ================= TESTS (messages) =================

(h/deftest-async test-core-on-message-set-settings
  (testing "test core/on-message :set-settings"
    (h/with-mock-js-chrome in-memory-storage
      (let [new-settings (assoc default-settings
                                :rotki-refresh-data-min 10)]
        (p/chain (promise-send-message :set-settings new-settings) 
                 #(do (is (= % {:data   new-settings
                                :status "success"
                                :action "set-settings"}))
                      (is (= (:settings @in-memory-storage) new-settings))))))))

(h/deftest-async test-core-on-message-get-settings
  (testing "test core/on-message :get-settings"
    (h/with-mock-js-chrome in-memory-storage
      (p/chain (core/on-install {:reason "install"})
               #(promise-send-message :get-settings) 
               #(is (= % {:data   default-settings
                          :status "success"
                          :action "get-settings"}))))))

(h/deftest-async test-core-on-message-get-rotki-data
  (testing "test core/on-message :get-rotki-data"
    (h/with-mock-date mock-date
      (h/with-mock-js-chrome in-memory-storage
        (h/with-http-stubs stubs
          (p/chain (core/on-install {:reason "install"})
                   #(promise-send-message :get-rotki-data) 
                   #(do (is (= (-> % :status) "success"))
                        (is (= (-> % :action) "get-rotki-data"))
                        (is (= (-> % :data :connected) true))
                        (is (= (-> % :data :snapshot-at) mock-date-epoch))
                        (is (= (-> % :data :data :total-balance) "12584.75"))
                        (is (= (-> % :data :data :assets :ETH :amount) "2.5")))))))))


;; [TODO]
;; - test cache-image and fetch-image

;; ================= TESTS (alarms) =================

(h/deftest-async test-core-on-alarm-tick-get-rotki-data
  (testing "test core/on-alarm-tick :get-rotki-data. Ensure the alams properly set data in the cache storage"
    (h/with-mock-date mock-date
      (h/with-mock-js-chrome in-memory-storage
        (h/with-http-stubs stubs
          (p/chain (core/on-install {:reason "install"})
                   #(core/on-alarm-tick :get-rotki-data)
                   #(do (is (= (-> @in-memory-storage :cache_rotki-data :started-at) mock-date-epoch))
                        (is (= (-> @in-memory-storage :cache_rotki-data :ttl)
                               (-> default-settings :rotki-snapshot-ttl-min (* 60))))
                        (is (= (-> @in-memory-storage :cache_rotki-data :data :total-balance) "12584.75")))))))))