(ns rotki-extension.common.config)

;; --- Environment

(goog-define env "development")

(defn- get-env
  []
  (keyword env))

(defn env?
  [e]
  (= (get-env) e))

(defn dev? []
  (env? :development))

(defn production? []
  (env? :production))

(defn test? []
  (env? :test))

;; --- Config

(def config
  {:common      {:default-settings {:rotki-endpoint         "http://localhost:4242"
                                    :rotki-timeout-sec      60
                                    :rotki-snapshot-ttl-min 30
                                    :rotki-refresh-data-min 5
                                    :use-mocked-data?       false
                                    :theme                  "light"
                                    :hide-zero-balances     true}

                 ;; DB
                 :init-db          {:root/loading?       true
                                    :root/page           :home
                                    :root/settings       {:theme                  "light"
                                                          :rotki-endpoint         ""
                                                          :rotki-timeout-sec      0
                                                          :rotki-refresh-data-min 0
                                                          :hide-zero-balances     true}
                                    :rotki/connected?    false
                                    :rotki/snapshot-at   0
                                    :rotki/total-balance 0
                                    :rotki/assets        {}}

                 ;; i18n configuration
                 :i18n             {:dictionary {:en {:navbar-top/title                        "rotki"
                                                      :navbar-bottom/assets                    "Assets"
                                                      :navbar-bottom/accounts                  "Accounts"
                                                      :navbar-bottom/settings                  "Settings"
                                                      :root/not-connected                      "Not connected to Rotki"
                                                      :root/not-connected-desc                 "Please check your settings and make sure your backend is running.    "
                                                      :root/not-connected-button               "Try again"
                                                      :setting/home:last-update                "Last update: %1"
                                                      :setting/home:column:name                "Name"
                                                      :setting/home:column:amount              "Amount"
                                                      :setting/home:column:usd                 "USD value"
                                                      :setting/title                           "Settings"
                                                      :setting/form:endpoint                   "Rotki server endpoint"
                                                      :setting/form:endpoint:tooltip           "The URL of your Rotki backend."
                                                      :setting/form:snapshot-ttl               "Snapshot TTL (minutes)"
                                                      :setting/form:snapshot-ttl:tooltip       "How long to keep data in cache before invalidating it."
                                                      :setting/form:timeout                    "Rotki server timeout (minutes)"
                                                      :setting/form:timeout:tooltip            "How long to wait for a response from the server."
                                                      :setting/form:background-refresh         "Background refresh (minutes)"
                                                      :setting/form:background-refresh:tooltip "How often to refresh the data in the background."
                                                      :setting/form:theme                      "Theme"
                                                      :setting/form:hide-zero-balances         "Hide zero balances"
                                                      :setting/form:use-mocked-data?           "Use mock data"
                                                      :setting/form:save                       "Save"
                                                      :missing                                 "Missing translation"}
                                                 :fr {}}}

                 ;; Theme (Daisy UI)
                 :theme            {:light "light"
                                    :dark  "dark"}}

   ;; Override common config when env=development
   :development {:default-settings {:rotki-endpoint         "http://localhost:4242"
                                    :rotki-timeout-sec      60
                                    :rotki-snapshot-ttl-min 5
                                    :rotki-refresh-data-min 1
                                    :use-mocked-data?       false
                                    :theme                  "light"
                                    :hide-zero-balances     true}}

   ;; Override common config when env=test
      :test        {:default-settings {:rotki-endpoint         "http://localhost:4242"
                                       :rotki-timeout-sec      60
                                       :rotki-snapshot-ttl-min 5
                                       :rotki-refresh-data-min 1
                                       :use-mocked-data?       true
                                       :theme                  "light"
                                       :hide-zero-balances     true}}

   ;; Override common config when env=production
   :production  {:default-settings {:rotki-endpoint         "http://localhost:4242"
                                    :rotki-timeout-sec      60
                                    :rotki-snapshot-ttl-min 30
                                    :rotki-refresh-data-min 5
                                    :use-mocked-data?       false
                                    :theme                  "light"
                                    :hide-zero-balances     true}}})

(defn- get-config
  [config env]
  (merge (:common config)
         (get config (keyword env))))

(defn read
  ([& path]
   (get-in (read) path))
  ([]
   (get-config config (get-env))))