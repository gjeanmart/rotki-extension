(ns rotki-extension.units-tests.sw.cache-test
  (:require [cljs.test :refer-macros [use-fixtures is testing]]
            ["timekeeper" :as timekeeper]
            [date-fns :as date-fns]
            [promesa.core :as p]
            [rotki-extension.sw.cache :as cache]
            [rotki-extension.test-helpers :as h]))

(def in-memory-storage (atom {}))
(def mock-date (js/Date. "2023-01-01T00:00:00.000"))
(def mock-date-epoch (date-fns/getUnixTime mock-date))
(def mock-date-30sec-after (js/Date. "2023-01-01T00:00:30.000"))
(def mock-date-1day-after (js/Date. "2023-01-02T00:00:00.000"))

(use-fixtures :each
  {:before #(reset! in-memory-storage {})})

;; ================= TESTS =================

(h/deftest-async cache-write
  (testing "test cache/write"
    (h/with-mock-date mock-date
      (h/with-mock-js-chrome in-memory-storage
        (p/chain (cache/write :foo "bar" {:ttl 60})
                 #(is (= % {:data       "bar"
                            :started-at mock-date-epoch
                            :ttl        60})))))))

(h/deftest-async cache-read-still-valid
  (testing "test cache-read still valid"
    (h/with-mock-date mock-date
      (h/with-mock-js-chrome in-memory-storage
        (p/chain (cache/write :foo "bar" {:ttl 60})
                 #(.. timekeeper (freeze mock-date-30sec-after))
                 #(cache/read :foo)
                 #(is (= % {:data       "bar"
                            :started-at mock-date-epoch
                            :ttl        60})))))))

(h/deftest-async cache-read-invalidated-by-ttl
  (testing "test cache-read invalidated by ttl"
    (h/with-mock-date mock-date
      (h/with-mock-js-chrome in-memory-storage
        (p/chain (cache/write :foo "bar" {:ttl 60})
                 #(.. timekeeper (freeze mock-date-1day-after))
                 #(cache/read :foo)
                 #(is (nil? %)))))))

(h/deftest-async cache-read-ignore-ttl
  (testing "test cache-read ignore ttl"
    (h/with-mock-date mock-date
      (h/with-mock-js-chrome in-memory-storage
        (p/chain (cache/write :foo "bar" {:ttl 60})
                 #(.. timekeeper (freeze mock-date-1day-after))
                 #(cache/read :foo {:ignore-ttl? true})
                 #(is (= % {:data       "bar"
                            :started-at mock-date-epoch
                            :ttl        60})))))))

(h/deftest-async cache-remove
  (testing "test cache-remove"
    (h/with-mock-date mock-date
      (h/with-mock-js-chrome in-memory-storage
        (p/chain (cache/write :foo "bar" {:ttl 60})
                 #(cache/remove :foo )
                 #(cache/read :foo)
                 #(is (nil? %)))))))