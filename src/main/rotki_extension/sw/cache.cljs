(ns rotki-extension.sw.cache
   (:refer-clojure :exclude [remove])
   (:require [promesa.core :as p]
             [rotki-extension.common.chrome-extension :as chrome-extension]
             [rotki-extension.common.date :as date]))

 (def prefix "cache_")
 (def default-ttl 60) ;; 1 min

(defn- make-storage-key 
  [key]
  (-> prefix
      (str (name key))
      keyword))
 
 (defn read
   [key & [{:keys [ignore-ttl?]}]]
   (p/let [{:keys [data started-at ttl]} (chrome-extension/storage-get (make-storage-key key))]
     (when (and data (or ignore-ttl?
                         (< (date/now) (+ started-at ttl))))
       {:data       data
        :started-at started-at
        :ttl        ttl})))
            
 (defn write
   [key value & [{:keys [ttl]
                  :or   {ttl default-ttl}}]]
   (p/chain (chrome-extension/storage-set (make-storage-key key)
                                          {:data       value
                                           :started-at (date/now)
                                           :ttl        ttl})
            #(read key)))
     
 (defn remove
  [key]
  (chrome-extension/storage-set (make-storage-key key) nil))