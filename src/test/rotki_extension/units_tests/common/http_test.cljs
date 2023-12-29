(ns rotki-extension.units-tests.common.http-test
  (:require [cljs.test :refer-macros [use-fixtures is testing]]
            [promesa.core :as p]
            [rotki-extension.common.http :as http]
            [rotki-extension.test-helpers :as h]))

(defonce mock-response-ok {:result "Mocked response"})
(defonce mock-response-ok2 {:result "Mocked response2"})
(defonce mock-response-ko {:error "Simulated error"})

(use-fixtures :each
  {:before h/clean-all-stubs})


;; ================= TESTS =================

(h/deftest-async test-get-request-success
  (testing "test http/get with a successful response"
    (h/with-http-stubs [{:url  "http://example.com"
                        :path "/test"
                        :body mock-response-ok}]
      (p/chain (http/get {:url "http://example.com/test"})
               #(is (= % mock-response-ok))))))

(h/deftest-async test-get-multiple-request-success
  (testing "test http/get with a successful response"
    (h/with-http-stubs [{:url  "http://example.com"
                         :path "/test1"
                         :body mock-response-ok}
                        {:url  "http://example.com"
                         :path "/test2"
                         :body mock-response-ok2}]
      (p/chain (http/get {:url "http://example.com/test1"})
               #(is (= % mock-response-ok))
               #(http/get {:url "http://example.com/test2"})
               #(is (= % mock-response-ok2))))))

(h/deftest-async test-post-request-success
  (testing "test http/post with a successful response"
    (h/with-http-stubs [{:url    "http://example.com"
                        :path   "/test"
                        :method :post
                        :body   mock-response-ok}]
      (p/chain (http/post {:url "http://example.com/test" :body {:foo "bar"}})
               #(is (= % mock-response-ok))))))

(h/deftest-async test-get-request-error
  (testing "test http/get with a failure response"
    (h/with-http-stubs [{:url    "http://example.com"
                        :path   "/error"
                        :status 500
                        :body   mock-response-ko}]
      (-> (http/get {:url "http://example.com/error"})
          (p/catch (fn [result]
                     (is (= result mock-response-ko))))))))

;; [TODO]
;; - test timeout
;; - test txt and blob result

