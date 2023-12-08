(ns rotki-extension.common.config)

;; --- Environment

(goog-define env "development")

(defn- get-env
  []
  (keyword env))

(defn env?
  "Return true if the given matches the running env."
  [e]
  (= (get-env) e))

;; --- Config

(def config
  {:common      {;; Rotki 
                 :default-settings {:rotki-endpoint         "http://localhost:4242"
                                    :rotki-timeout-sec      60  ;; 1 min
                                    :rotki-snapshot-ttl     1800 ;; 30 min
                                    :rotki-refresh-data-min 15  
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
                                                      :setting/form:snapshot-ttl               "Snapshot TTL (seconds)"
                                                      :setting/form:snapshot-ttl:tooltip       "How long to keep data in cache before invalidating it."
                                                      :setting/form:timeout                    "Rotki server timeout (seconds)"
                                                      :setting/form:timeout:tooltip            "How long to wait for a response from the server."
                                                      :setting/form:background-refresh         "Background refresh (minutes)"
                                                      :setting/form:background-refresh:tooltip "How often to refresh the data in the background."
                                                      :setting/form:theme                      "Theme"
                                                      :setting/form:hide-zero-balances         "Hide zero balances"
                                                      :setting/form:save                       "Save"
                                                      :missing                                 "Missing translation"}
                                                 :fr {}}}

                 ;; Theme (Daisy UI)
                 :theme            {:light "light"
                                    :dark  "dark"}}
   :development {}
   :staging     {}
   :production  {}})

(defn- get-config
  "Returns the config for the current environment."
  [config env]
  (merge (:common config)
         (get config (keyword env))))

(defn read
  "Read a key from the config for the given path.
  Example:
  (read)
  (read :firebase :projectId)"
  ([& path]
   (get-in (read) path))
  ([]
   (get-config config (get-env))))