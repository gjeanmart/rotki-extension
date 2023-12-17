(ns rotki-extension.units-tests.common.date-test
  (:require ["date-fns" :as date-fns]
            [cljs.test :refer-macros [deftest is testing]]
            [rotki-extension.common.date :as date]
            [rotki-extension.test-helpers :as h]))

(def mock-date (js/Date. "2023-01-01T00:00:00.000"))
(def mock-date-epoch (date-fns/getUnixTime mock-date))

;; ================= TESTS =================

(deftest test-date-now
  (testing "test date/now"
    (h/with-mock-date mock-date
      (is (= (date/now) mock-date-epoch)))))

(deftest test-date-format
  (testing "test date/format"
    (is (= (date/format mock-date-epoch) "2023-01-01 00:00"))
    (is (= (date/format mock-date-epoch "yyyy-MM-dd HH:mm:ss") "2023-01-01 00:00:00"))
    (is (= (date/format mock-date-epoch "yyyy-MM-dd") "2023-01-01"))))
