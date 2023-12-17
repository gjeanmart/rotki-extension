(ns rotki-extension.units-tests.common.chrome-extension-test
  (:require [cljs.test :refer-macros [use-fixtures deftest is testing]]
            [promesa.core :as p]
            [rotki-extension.common.chrome-extension :as chrome-extension]
            [rotki-extension.test-helpers :as h]))

(def in-memory-storage (atom {}))

(use-fixtures :each
  {:before #(reset! in-memory-storage {})})

;; ================= TESTS - RUNTIME =================
;; [Note] those are pretty much useless tests since they are just wrappers around js/chrome

(deftest test-onInstalled
  (testing "chrome-extension/on-install"
    (h/with-mock-js-chrome in-memory-storage
      (chrome-extension/on-install (fn [result]
                                     (is (= result {:foo "bar"})))))))

(deftest test-onStartup
  (testing "chrome-extension/on-startup"
    (h/with-mock-js-chrome in-memory-storage
      (chrome-extension/on-startup (fn [result]
                                     (is (= result {:foo "bar"})))))))

(deftest test-onMessage
  (testing "chrome-extension/on-message"
    (h/with-mock-js-chrome in-memory-storage
      (chrome-extension/on-message (fn [result]
                                     (is (= result {:foo "bar"})))))))


;; ================= TESTS - STORAGE =================

(h/deftest-async test-storage-set-single-key
  (testing "chrome-extension/storage-set (single key)"
    (h/with-mock-js-chrome in-memory-storage
      (p/chain (chrome-extension/storage-set :foo "bar")
               #(is (= @in-memory-storage {:foo "bar"}))))))

(h/deftest-async test-storage-set-multiple-keys
  (testing "chrome-extension/storage-set (multiple keys)"
    (h/with-mock-js-chrome in-memory-storage
      (p/chain (chrome-extension/storage-set :key1 "bar" :key2 42 :key3 true)
               #(is (= @in-memory-storage {:key1 "bar" :key2 42 :key3 true}))))))

(h/deftest-async test-storage-get-single-key
  (testing "chrome-extension/storage-get (single-key)"
    (h/with-mock-js-chrome in-memory-storage
      (swap! in-memory-storage assoc :foo "bar")
      (p/chain (chrome-extension/storage-get :foo)
               #(is (= % "bar"))))))

(h/deftest-async test-storage-get-multiple-keys
  (testing "chrome-extension/storage-get (multiple keys)"
    (h/with-mock-js-chrome in-memory-storage
      (swap! in-memory-storage assoc :key1 "bar" :key2 42 :key3 true)
      (p/chain (chrome-extension/storage-get :key1 :key2 :key3)
               #(is (= % ["bar" 42 true]))))))

(h/deftest-async test-storage-get-not-found
  (testing "chrome-extension/storage-get (key not found)"
    (h/with-mock-js-chrome in-memory-storage
      (p/chain (chrome-extension/storage-get :foo)
               #(is (nil? %))))))

(h/deftest-async test-storage-set-single-key-and-read-it
  (testing "chrome-extension/storage-set (single key) and chrome-extension/storage-get"
    (h/with-mock-js-chrome in-memory-storage
      (p/chain (chrome-extension/storage-set :foo "bar")
               #(chrome-extension/storage-get :foo)
               #(is (= % "bar"))))))