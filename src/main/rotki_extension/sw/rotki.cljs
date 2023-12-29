(ns rotki-extension.sw.rotki
  (:require [promesa.core :as p] 
            [rotki-extension.common.http :as http]
            [rotki-extension.common.utils :as ut]
            [rotki-extension.sw.cache :as cache]
            [rotki-extension.sw.log :as log]
            [shadow.resource :as rc]
            [rotki-extension.common.date :as date]))

;; ------ CONSTANT ------

(def cache-key :rotki-data)

;; ------ MOCKED DATA ------

(defn mock-request
  [path]
  (condp = path
    "/api/1/ping"                (ut/json->clj (rc/inline "./mock/api_1_ping-response.json"))
    "/api/1/balances"            (ut/json->clj (rc/inline "./mock/api_1_balances-response.json"))
    "/api/1/assets/mappings"     (ut/json->clj (rc/inline "./mock/api_1_assets_mappings-response.json"))
    "/api/1/statistics/netvalue" (ut/json->clj (rc/inline "./mock/api_1_statistics-netvalue-response.json"))
    (throw (ex-info "No mock found" {:path path}))))

;; ------ ROTKI API ------

(def api-version 1)

(def default-headers {"Content-Type" "application/json"})

(defn- format-id
  [id]
  (-> id
      keyword
      str
      ;; remove `:` from the beginning of the id
      (subs 1)))

(defn- execute 
  [{:keys [settings request method]}]
  (if (:use-mocked-data? settings)
    (-> request :url ut/get-url-path mock-request)
    (condp = method
      :get  (http/get request)
      :post (http/post request))))

(defn- make-request
  [{:keys [rotki-endpoint rotki-timeout-sec]} path & [params body headers]]
  (let [url (cond->          rotki-endpoint 
              true           (str  "/api/" api-version)
              true           (str path)
              (some? params) (str "?" (ut/->query-params params)))]
    {:url     url
     :timeout (* rotki-timeout-sec 1000)
     :headers (merge default-headers headers)
     :body    body}))

(defn- handle-response 
  [{:keys [result]}]
  result)

(defn- handle-error
  [{:keys [message]}]
  (p/rejected message))

(defn ping
  "See https://rotki.readthedocs.io/en/stable/api.html#get--api-(version)-ping"
  [settings]
  (-> (p/chain (make-request settings "/ping")
               #(execute {:settings settings :method :get :request %})
               handle-response)
      (p/catch handle-error)))
  
(defn balances
  "See https://rotki.readthedocs.io/en/stable/api.html#get--api-(version)-balances"
  [settings]
  (-> (p/chain (make-request settings  "/balances" {:ignore_cache false
                                                    :save_data    false})
               #(execute {:settings settings :method :get :request %})
               handle-response)
      (p/catch handle-error)))

(defn get-asset-identifiers-mappings
  "See https://rotki.readthedocs.io/en/stable/api.html#get-asset-identifiers-mappings"
  [settings identifiers]
  (when (seq identifiers)
    (-> (p/chain (make-request settings  "/assets/mappings" {} {:identifiers (map format-id identifiers)})
                 #(execute {:settings settings :method :post :request  %})
                 handle-response)
        (p/catch handle-error))))

(defn statistics-netvalue
  "See https://rotki.readthedocs.io/en/latest/api.html#statistics-for-netvalue-over-time"
  [settings]
  (-> (p/chain (make-request settings  "/statistics/netvalue")
               #(execute {:settings settings :method :get :request %})
               handle-response)
      (p/catch handle-error)))

(defn get-asset-logo
  "See https://rotki.readthedocs.io/en/stable/api.html#get--api-(version)-assets-icon"
  [settings {:keys [id image_url]}]
  (if image_url
    image_url
    (-> settings
        (make-request "/assets/icon" {:asset id})
        :url)))


;; ;; ------- ROTKI FN -------

(defn- fetch-data-make-response
  [{:keys [settings net_usd assets assets-identifiers-mappings]}]
  {:total-balance net_usd
   :assets        (->> (:assets assets-identifiers-mappings)
                       ;; merge assets balances `assets` with asset metadata `assets-identifiers-mappings`
                       (merge-with merge assets)
                       ;; convert to vector and format id
                       (reduce-kv #(conj %1 (assoc %3 :id (format-id %2))) [])
                       ;; attach image_url
                       (map #(assoc % :image_url (get-asset-logo settings %)))
                       ;; sort by usd_value
                       (sort-by #(js/parseFloat (:usd_value %)) >)
                       ;; transform as sorted map
                       (reduce #(assoc %1 (:id %2) %2) (sorted-map)))})

(defn- fetch-data-error 
  [error success _failure]
  (log/warning "Error while fetching data from rotki: " error)
  (p/let [{cache-data :data
           cache-date :started-at} (cache/read cache-key {:ignore-ttl? true})]
    (success {:data        cache-data
              :snapshot-at cache-date
              :connected   false})))

(defn fetch-data
  ;; [TODO] review this big function
  [{:keys [settings force-refresh? success failure]
    :or   {force-refresh? false}}]
  (-> (p/let [{cache-data :data cache-date :started-at} (cache/read cache-key)
             ;; Ping to verify if rotki is running
              _ (ping settings)]

        (-> (if (and (not force-refresh?) cache-data)
              ;; Found data in cache
              (success {:data        cache-data
                        :snapshot-at cache-date
                        :connected   true})

              ;; No data in cache or TTL expired
              (p/let [;; Fetch data from rotki
                      {:keys [assets net_usd]}    (balances settings)
                      assets-identifiers-mappings (get-asset-identifiers-mappings settings (keys assets))
                      ;; Format respomse
                      data                        (fetch-data-make-response {:settings                    settings
                                                                             :net_usd                     net_usd
                                                                             :assets                      assets
                                                                             :assets-identifiers-mappings assets-identifiers-mappings})
                      ;; Cache data 
                      {snapshot-at :started-at}   (cache/write cache-key data
                                                               {:ttl (* (:rotki-snapshot-ttl-min settings) 60)})]
                (success {:data        data
                          :snapshot-at snapshot-at
                          :connected   true})))
            (p/catch #(fetch-data-error % success failure))))
      (p/catch #(fetch-data-error % success failure))))

(defn get-trend 
  [{:keys [settings success failure]}]
  (-> (p/let [nb-days              7 ;; [TODO] make this configurable
              trend-date           (date/move (date/now) {:days (- nb-days)})
              {:keys [times data]} (statistics-netvalue settings)
              closest-time         (reduce (fn [prev curr]
                                             (if (< (Math/abs (- curr trend-date)) (Math/abs (- prev trend-date)))
                                               curr
                                               prev)) 0 times)
              closest-time-index   (->> times
                                        (map-indexed vector)
                                        (filter #(= (second %) closest-time))
                                        (ffirst))
              prev-value           (nth data closest-time-index)
              last-value           (last data)]
        (success (if (> last-value prev-value) :up :down)))
      (p/catch #(failure %))))