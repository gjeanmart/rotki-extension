(ns rotki-extension.units-tests.common.config-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [rotki-extension.common.config :as config]))


;; ================= TESTS =================

(deftest test-config-test? 
  []
  (testing "Current environment is :test"
    (is true (config/test?))))

(deftest test-config-read
  (testing "Current configuration is the :test one"
    (let [config (config/read :default-settings)]
      (is (= "http://localhost:4242" (:rotki-endpoint config)))
      (is (= 60                      (:rotki-timeout-sec config)))
      (is (= 5                       (:rotki-snapshot-ttl-min config)))
      (is (= 1                       (:rotki-refresh-data-min config)))
      (is (= true                    (:use-mocked-data? config)))
      (is (= "light"                 (:theme config)))
      (is (= true                    (:hide-zero-balances config))))))
