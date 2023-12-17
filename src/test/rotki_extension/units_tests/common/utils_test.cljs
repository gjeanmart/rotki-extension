(ns rotki-extension.units-tests.common.utils-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [goog.object]
            [rotki-extension.common.utils :as utils]))

;; ================= TESTS - JSON =================

(deftest test-js->clj-conversion
  (testing "test j->c c->j json->clj clj->json"
    (is (= (utils/j->c #js {:foo "bar"}) {:foo "bar"}))
    (is (goog.object/equals (utils/c->j {:foo "bar"}) #js {:foo "bar"}))
    (is (= (utils/json->clj "{\"foo\":\"bar\"}") {:foo "bar"}))
    (is (= (utils/clj->json {:foo "bar"}) "{\"foo\":\"bar\"}"))))


;; ================= TESTS - URL =================

(deftest test-get-url-path
  (testing "test get-url-path"
    (is (= (utils/get-url-path "http://example.com/foo/bar")
           "/foo/bar"))))

(deftest test->query-params
  (testing "test ->query-params"
    (is (= (utils/->query-params {:a 1 :b "str" :c true})
           "a=1&b=str&c=true"))))


;; ================= TESTS - CRYPTO =================

(deftest test-sha256
  (testing "test sha256"
    (let [msg "hello world"]
      (is (= (utils/sha256 msg)
             "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9"))
      (is (= (utils/sha256 msg {:encoding :hex})
             "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9"))
      (is (= (utils/sha256 msg {:encoding :base64})
             "uU0nuZNNPgilLlLX2n2r+sSE7+N6U4DukIj3rOLvzek=")))))


;; ================= TESTS - NUMBER =================

(deftest format-currency
  (testing "test format-currency"
    (is (= (utils/format-currency 1234.56 {:currency :USD})
           "$1,234.56"))))


;; ================= TESTS - STYLE =================

;;[TODO]: test style