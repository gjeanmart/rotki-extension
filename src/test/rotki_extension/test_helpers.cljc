(ns rotki-extension.test-helpers
  #?(:cljs (:require ["nock" :as nock]
                     ["cross-fetch/polyfill" :as fetch]
                     [rotki-extension.common.utils :as ut]
                     [promesa.core :as p]))
  #?(:cljs (:require-macros [rotki-extension.test-helpers]))
  #?(:clj (:require [cljs.test :as test]
                    [promesa.core :as p])))

#?(:cljs
   (do
     (defn stub-http
       [base-url {:keys [action path status match-body body query]
                  :or   {match-body any?
                         action     :get
                         status     200}}]
       (let [wrapped-match-body (comp match-body ut/j->c)]
         (cond-> (nock base-url)
           (= :get action)    (.get path wrapped-match-body)
           (= :post action)   (.post path wrapped-match-body)
           (= :patch action)  (.patch path wrapped-match-body)
           (= :delete action) (.delete path wrapped-match-body)
           query              (.query (clj->js query))
           :then              (.reply status (clj->js body)))))

     (defn clean-all-stubs
       []
       (.cleanAll nock))
     
     (defn load-fetch 
       []
       fetch)
     
     (defn mock-js-chrome
       [in-memory-storage]
       (ut/c->j {:runtime {:onInstalled {:addListener (fn [callback] (callback #js {:foo "bar"}))}
                           :onStartup   {:addListener (fn [callback] (callback #js {:foo "bar"}))}
                           :onMessage   {:addListener (fn [callback] (callback #js {:foo "bar"}))}}
                 :storage {:local {:get (fn [keys] (->> keys
                                                        (map keyword)
                                                        (select-keys @in-memory-storage)
                                                        ut/c->j
                                                        p/resolved))
                                   :set (fn [obj] (swap! in-memory-storage merge (ut/j->c obj))
                                          (p/resolved true))}}}))))

#?(:clj
   (do
     (defmacro deftest-async
       [name & body]
       `(test/deftest ~name
          (test/async done#
                      (-> (p/do! ~@body)
                          (p/catch (fn [error#]
                                     (test/is (nil? error#))))
                          (p/finally done#)))))
     
     (defmacro with-stub-http
       [{:keys [url path body action status] :or {path "/" action :get status 200 }} 
        & body']
       `(with-redefs [js/fetch (load-fetch)]
          (stub-http ~url {:action ~action
                           :path   ~path
                           :status ~status
                           :body   ~body})
          ~@body'))

     (defmacro with-mock-js-chrome
       [storage & body]
       `(do (set! js/chrome (fn []))
            (with-redefs [js/chrome (mock-js-chrome ~storage)]
              ~@body)))))