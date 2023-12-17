(ns rotki-extension.test-helpers
  #?(:cljs (:require ["nock" :as nock]
                     ["cross-fetch/polyfill" :as fetch]
                     ["timekeeper" :as timekeeper]
                     [rotki-extension.common.utils :as ut]
                     [promesa.core :as p]))
  #?(:cljs (:require-macros [rotki-extension.test-helpers]))
  #?(:clj (:require [cljs.test :as test]
                    [promesa.core :as p])))

#?(:cljs
   (do 
     (def fetch' fetch) 
     (def timekeeper' timekeeper)

     (defn stub-http
       [base-url {:keys [method path status body query]}]
       (cond-> (nock base-url)
         (= :get method)    (.get path)
         (= :post method)   (.post path)
         (= :patch method)  (.patch path)
         (= :delete method) (.delete path)
         query              (.query (clj->js query))
         :then              (.reply status (ut/clj->json body))))

     (defn clean-all-stubs
       []
       (.cleanAll nock))
     
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
                                   :set (fn [obj]
                                          (swap! in-memory-storage merge (ut/j->c obj))
                                          (p/resolved true))
                                   :clear (fn []
                                            (reset! in-memory-storage {})
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
     
     (defmacro with-http-stubs
       [stubs & body]
       `(with-redefs [js/fetch fetch']
          (doall
           (for [stub# ~stubs]
             (stub-http (:url stub#) {:method (get stub# :method :get)
                                      :path   (get stub# :path "/")
                                      :status (get stub# :status 200)
                                      :query  (get stub# :query {})
                                      :body   (get stub# :body {})})))
          ~@body))
     
     (defmacro with-mock-js-chrome
       [storage & body]
       `(do (set! js/chrome (mock-js-chrome ~storage))
            ~@body))

     (defmacro with-mock-date
       [date & body]
       `(p/do! (-> timekeeper' (.freeze ~date))
               ~@body
               (-> timekeeper' (.reset))))))