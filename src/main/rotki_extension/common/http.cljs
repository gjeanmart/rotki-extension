(ns rotki-extension.common.http
  (:refer-clojure :exclude [get])
  (:require [rotki-extension.common.utils :as ut]
            [promesa.core :as p]))



(def default-timeout 10000)

(defn- base
  [{:keys [method url headers body timeout output]
    :or   {output :json timeout default-timeout}}]
  (let [controller (js/AbortController.)
        timeout-id (js/setTimeout #(.. controller (abort)) timeout)]
    (p/create
     (fn [resolve reject]
       (-> (p/let [request  (cond-> {:method  method}
                              headers (assoc :headers headers)
                              body    (assoc :body   (ut/clj->json body))
                              true    (assoc :signal (.. controller -signal)))
                   response  (js/fetch url (ut/c->j request))]
             (js/clearTimeout timeout-id)
             (if (.-ok response)
               ;; Success
               (condp = output
                 :json (p/chain (.. response (json))
                                #(resolve (ut/j->c %)))
                 :text (p/chain (.. response (text))
                                #(resolve %))
                 :blob (p/chain (.. response (blob))
                                (fn [blob]
                                  (let [reader (js/FileReader.)]
                                    (set! (.. reader -onloadend) #(resolve (.. reader -result))) 
                                    (.. reader (readAsDataURL blob))))))
               ;; Failure
               (p/chain (.. response (json))
                        #(reject (ut/j->c %)))))
           
           (p/catch (fn [error]
                      (if (= "AbortError" (.-name error))
                        (reject {:message "Request timed out"})
                        (reject {:message "Server not responding"
                                 :error    (.-message error)
                                 :url      url})))))))))

(defn get
  [opts]
  (base (assoc opts :method "GET")))

(defn post
  [opts]
  (base (assoc opts :method "POST")))


