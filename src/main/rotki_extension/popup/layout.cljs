(ns rotki-extension.popup.layout
  (:require [re-frame.core :as rf]
            [rotki-extension.popup.components.dropdown :as dropdown]
            [rotki-extension.popup.components.icons :as icons]
            [rotki-extension.popup.components.loader :as loader]
            [rotki-extension.popup.components.typography :as typography]
            [rotki-extension.popup.pages.accounts.view :as accounts]
            [rotki-extension.popup.pages.home.view :as home]
            [rotki-extension.popup.pages.settings.view :as settings]
            [rotki-extension.popup.utils.i18n :refer [tr]]
            [rotki-extension.common.config :as config]))

(defn- loading
  []
  [:div.flex.flex-col.items-center.h-full.w-full.gap-2
   [loader/ring {:size :large}]])

(defn- top-navbar
  []
  [:div.navbar.flex.items-center.justify-between
   [typography/base {:weight :extrabold
                     :size   :3xl
                     :type   :primary}
    (tr [:navbar-top/title])]

   [:div.flex-none.gap-2
    [dropdown/base {:options [[:div {:class "w-[150px]"}
                               [:a {:on-click #(rf/dispatch [:core/boot {:force-refresh? true}])}
                                [:div.flex.gap-2.items-center
                                 [icons/arrows-clockwise]
                                 (tr ["Refresh data"])]]]
                              [:div {:class "w-[150px]"}
                               [:div.flex.gap-2.items-center
                                [icons/stamp]
                                (tr ["Version %1"] [(config/get-version)])]]]
                    :tabindex 0}
     [icons/rotki]]]])

(defn- bottom-navbar
  [current-page]
  [:div.btm-nav
   [:button {:class (if (= current-page :home) "active" "")
             :on-click #(rf/dispatch [:root/navigate :home])}
    [icons/money]
    [typography/base (tr [:navbar-bottom/assets])]]
   [:button {:class (if (= current-page :settings) "active" "")
             :on-click #(rf/dispatch [:root/navigate :settings])}
    [icons/gear]
    [typography/base (tr [:navbar-bottom/settings])]]])

(defn- not-connected
  []
  [:div.alert.flex.flex-col.gap-1.mx-2.shadow-sm.p-1
   {:class "w-[95%] h-[86px]"}
   [:div.flex.flex-row.justify-center.w-full.gap-1
    [icons/warning-circle {:size 20 :color :red}]
    [typography/error {:weight :bold}
     (tr [:root/not-connected])]]
   [:div.w-full.gap-1
    [typography/base {:size :xs}
     (tr [:root/not-connected-desc])]
    [:span {:class "hover:underline cursor-pointer"
            :on-click #(rf/dispatch [:core/boot])}
     (tr [:root/not-connected-button])]]])

(defn- main-layout
  [{:keys [current-page loading? connected?]}]
  [:<>
   [top-navbar]

   [:div.overflow-y-hidden {:class "h-[436px] w-[320px]"}
    (cond
      ;; can still access "settings" page  even if loading
      (and loading? (not= current-page :settings))
      [loading]
      ;; ;; can still access "settings" page  even if not connected
      ;; (and (not connected?) (not= current-page :settings))
      ;; [not-connected]
      (= current-page :home)
      [:div.flex.flex-col.h-full.gap-2
       (when-not connected?
         [not-connected])
       [home/page]]

      (= current-page :accounts)
      [:div.flex.flex-col.h-full.gap-2
       (when-not connected?
         [not-connected])
       [accounts/page]]

      (= current-page :settings)
      [settings/page]
      :else
      [home/page])]

   [bottom-navbar current-page]])

(defn base
  []
  (let [current-page  @(rf/subscribe [:root/page])
        loading?      @(rf/subscribe [:root/loading?])
        connected?    @(rf/subscribe [:rotki/connected?])]
    [:div.artboard.phone-1.flex.flex-col
     {:data-theme @(rf/subscribe [:profile/theme])}
     [main-layout {:current-page current-page
                   :loading?     loading?
                   :connected?   connected?}]]))