(ns rotki-extension.units-tests.common.date-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [rotki-extension.common.date :as date]
            ["date-fns" :as date-fns]))

(defonce mock-date (js/Date. "2023-01-01T00:00:00.000") )
(defonce mock-date-epoch (date-fns/getUnixTime mock-date) )

;; ================= TESTS =================

(deftest test-date-now
  (testing "test date/now"
    (with-redefs [js/Date (fn [] mock-date)]
      (is (= (date/now) mock-date-epoch)))))

(deftest test-date-format
  (testing "test date/format"
    (is (= (date/format mock-date-epoch) "2023-01-01 00:00"))
    (is (= (date/format mock-date-epoch "yyyy-MM-dd HH:mm:ss") "2023-01-01 00:00:00"))
    (is (= (date/format mock-date-epoch "yyyy-MM-dd") "2023-01-01"))))
