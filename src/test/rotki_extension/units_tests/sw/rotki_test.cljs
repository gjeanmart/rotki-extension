(ns rotki-extension.units-tests.sw.rotki-test
  (:require [cljs.test :refer-macros [deftest use-fixtures is testing]]
            [date-fns :as date-fns]
            [promesa.core :as p]
            [rotki-extension.common.config :as config]
            [rotki-extension.sw.rotki :as rotki]
            [rotki-extension.test-helpers :as h]))

(def default-settings (config/read :default-settings))
(def in-memory-storage (atom {}))
(def mock-date (js/Date. "2023-01-01T00:00:00.000"))
(def mock-date-epoch (date-fns/getUnixTime mock-date))

(use-fixtures :each
  {:before #(do (h/clean-all-stubs)
                (reset! in-memory-storage {}))})


;; ================= TESTS (API) =================

(h/deftest-async test-rotki-api-ping
  (testing "test rotki/ping"
    (h/with-http-stubs [{:url    (:rotki-endpoint default-settings)
                         :path   "/api/1/ping"
                         :body   (rotki/mock-request "/api/1/ping")}]
      (p/chain (rotki/ping default-settings)
               #(is (true? %))))))

(h/deftest-async test-rotki-api-balances
  (testing "test rotki/balances"
    (h/with-http-stubs [{:url    (:rotki-endpoint default-settings)
                         :path   "/api/1/balances"
                         :query  {:ignore_cache false
                                  :save_data    false}
                         :body   (rotki/mock-request "/api/1/balances")}]
      (p/chain (rotki/balances default-settings)
               #(do (is (= (:net_usd %) "12584.75"))
                    (is (= (-> % :assets :ETH) {:amount                  "2.5"
                                                :usd_value               "5616.575000000001"
                                                :percentage_of_net_value "46.8268%"})))))))

(h/deftest-async test-rotki-api-asset-identifiers-mappings
  (testing "test rotki/asset-identifiers-mappings"
    (h/with-http-stubs [{:url    (:rotki-endpoint default-settings)
                         :path   "/api/1/assets/mappings"
                         :method :post
                         :body   (rotki/mock-request "/api/1/assets/mappings")}]
      (p/chain (rotki/get-asset-identifiers-mappings default-settings
                                                     ["ETH" "BTC" "eip155:1/erc20:0x6B175474E89094C44Da98b954EedeAC495271d0F"])
               #(do (is (= (-> % :assets :ETH :symbol) "ETH"))
                    (is (= (-> % :assets :BTC :symbol) "BTC"))
                    (is (= (-> % :assets :erc20:0x6B175474E89094C44Da98b954EedeAC495271d0F :symbol) "DAI")))))))

(h/deftest-async test-rotki-api-statistics-netvalue
  (testing "test rotki/statistics-netvalue"
    (h/with-http-stubs [{:url    (:rotki-endpoint default-settings)
                         :path   "/api/1/statistics/netvalue"
                         :body   (rotki/mock-request "/api/1/statistics/netvalue")}]
      (p/chain (rotki/statistics-netvalue default-settings)
               #(do (is (= (count (:times %)) 4))
                    (is (= (count (:data %)) 4)))))))

(deftest test-get-asset-logo
  (testing "Test rotki/get-asset-logo"
    (is (= (rotki/get-asset-logo default-settings {:id "ETH"})
           (str (:rotki-endpoint default-settings) "/api/1/assets/icon?asset=ETH")))
    (is (= (rotki/get-asset-logo default-settings {:id "eip155:1/erc20:0x6B175474E89094C44Da98b954EedeAC495271d0F"})
           (str (:rotki-endpoint default-settings) "/api/1/assets/icon?asset=eip155%3A1%2Ferc20%3A0x6B175474E89094C44Da98b954EedeAC495271d0F")))
    (is (= (rotki/get-asset-logo default-settings {:id "NFTX" :image_url "https://nftx.png"})
           "https://nftx.png"))))


;; ================= TESTS (fetch-data) =================

(h/deftest-async test-rotki-fetch-data-no-cache-connected
  (testing "test rotki/fetch-data (no cache - connected)"
    (h/with-mock-date mock-date
      (h/with-mock-js-chrome in-memory-storage
        (h/with-http-stubs [{:url    (:rotki-endpoint default-settings)
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
                             :body   (rotki/mock-request "/api/1/assets/mappings")}]
          (p/chain (p/create (fn [resolve reject]
                               (rotki/fetch-data {:settings       default-settings
                                                  :force-refresh? false
                                                  :success        #(resolve %)
                                                  :failure        #(reject %)})))
                   #(do (is (= mock-date-epoch (:snapshot-at %)))
                        (is (= (:connected %) true))
                        (is (= (-> % :data :total-balance) "12584.75"))
                        (is (= (-> % :data :assets (get "ETH")) {:amount                  "2.5"
                                                                 :usd_value               "5616.575000000001"
                                                                 :percentage_of_net_value "46.8268%"
                                                                 :name                    "Ethereum"
                                                                 :symbol                  "ETH"
                                                                 :asset_type              "own chain"
                                                                 :id                      "ETH"
                                                                 :image_url               (str (:rotki-endpoint default-settings) "/api/1/assets/icon?asset=ETH")})))))))))

(h/deftest-async test-rotki-fetch-data-no-cache-not-connected
  (testing "test rotki/fetch-data (no cache - not connected)"
    (h/with-mock-date mock-date
      (h/with-mock-js-chrome in-memory-storage
        (p/chain (p/create (fn [resolve reject]
                             (rotki/fetch-data {:settings       default-settings
                                                :force-refresh? false
                                                :success        #(resolve %)
                                                :failure        #(reject %)})))
                 #(do (is (nil? (:snapshot-at %)))
                      (is (= (:connected %) false))
                      (is (nil? (:data %)))))))))


;; [TODO]
;; - test with cache enabled

(h/deftest-async test-rotki-get-trend
  (testing "test rotki/get-trend"
    (h/with-mock-date mock-date
      (h/with-http-stubs [{:url  (:rotki-endpoint default-settings)
                           :path "/api/1/statistics/netvalue"
                           :body (rotki/mock-request "/api/1/statistics/netvalue")}]
        (p/chain (p/create (fn [resolve reject]
                             (rotki/get-trend {:settings default-settings
                                               :success  #(resolve %)
                                               :failure  #(reject %)})))
                 #(do (is (= :up %))))))))

(h/deftest-async test-rotki-get-trend-not-connected
  (testing "test rotki/get-trend-not-connected"
    (h/with-mock-date mock-date
      (p/chain (p/create (fn [resolve reject]
                           (rotki/get-trend {:settings default-settings
                                             :success  #(reject %)
                                             :failure  #(resolve %)})))
               #(do (is (= "Server not responding" %)))))))