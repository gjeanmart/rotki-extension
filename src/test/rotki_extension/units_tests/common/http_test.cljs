(ns rotki-extension.units-tests.common.http-test
    (:require [cljs.test :refer-macros [use-fixtures is testing]]
              [promesa.core :as p]
              [rotki-extension.common.http :as http]
              [rotki-extension.test-helpers :as h]))

  (defonce mock-response-ok {:result "Mocked response"})
  (defonce mock-response-ko {:error "Simulated error"})

  (use-fixtures :each
    {:before h/clean-all-stubs})
  

;; ================= TESTS =================

  (h/deftest-async test-get-request-success
    (testing "test http/get with a successful response"
      (h/with-stub-http {:url  "http://example.com"
                         :path "/test"
                         :body mock-response-ok}
        (p/chain (http/get {:url "http://example.com/test"})
                 #(is (= % mock-response-ok))))))

(h/deftest-async test-post-request-success
  (testing "test http/post with a successful response"
    (h/with-stub-http {:url    "http://example.com"
                       :path   "/test"
                       :action :post
                       :body   mock-response-ok}
      (p/chain (http/post {:url "http://example.com/test" :body {:foo "bar"}})
               #(is (= % mock-response-ok))))))

(h/deftest-async test-get-request-error
  (testing "test http/get with a failure response"
    (h/with-stub-http {:url    "http://example.com"
                       :path   "/error"
                       :status 500
                       :body   mock-response-ko}
      (-> (http/get {:url "http://example.com/error"})
          (p/catch (fn [result]
                     (is (= result mock-response-ko))))))))

;; [TODO]
;; - test timeout
;; - test txt and blob result