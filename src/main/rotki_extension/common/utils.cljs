(ns rotki-extension.common.utils
   (:require [clojure.string :as string]
             ["crypto-browserify" :as crypto]))


;; ---- JSON ----

 (defn j->c [o] (js->clj o :keywordize-keys true))
 (defn c->j [o] (clj->js o))
 (defn json->clj [s]  (-> s js/JSON.parse j->c))
 (defn clj->json [o] (-> o c->j js/JSON.stringify))


;; ---- URL ----
 
 (defn ->query-params
   [options]
   (.. (js/URLSearchParams. (c->j options))
       (toString)))

 
;; ---- CRYPTO ----

  (defn sha256
   [buffer & [{:keys [encoding] :or {encoding :hex}}]]
   (.. crypto
       (createHash "sha256")
       (update buffer)
       (digest (name encoding))))
 

;; ---- NUMBER ----

 (defn format-number
   [num {:keys [local style currency notation decimals]
         :or   {local "en"}}]
   (let [opts (cond-> {}
                decimals (assoc :minimumFractionDigits decimals)
                style    (assoc :style style)
                currency (assoc :currency currency)
                notation (assoc :notation notation))]
     (.. js/Intl
         (NumberFormat local
                       (c->j opts))
         (format (js/parseFloat num)))))

 (defn format-currency
   [num {:keys [currency] :as opts}]
   (format-number num
                  (assoc opts
                         :style :currency
                         :currency currency)))


;; ---- STYLE ----

(defn string->classes
  [classes]
  (cond
    (nil? classes)        []
    (string? classes)     (string/split classes #" ")
    (sequential? classes) classes
    :else                 (throw (ex-info "Invalid type" {:classes classes}))))

(defn classes->string
  [classes]
  (cond
    (nil? classes)        ""
    (string? classes)     classes
    (sequential? classes) (->> classes (remove string/blank?) (map name) (string/join " "))
    :else                 (throw (ex-info "Invalid type" {:classes classes}))))

(defn class-wrapper
  [{:keys [class] :as opts} converter & [default-classes]]
  (-> class
      (string->classes)
      (concat (or default-classes []))
      (converter opts)
      (classes->string)))