(ns rotki-extension.popup.components.typography
  (:require [rotki-extension.common.utils :as ut]))

(def default-classes [])

(defn- size->class
  [size]
  (condp = size
    :4xl  "text-4xl"
    :3xl  "text-3xl"
    :2xl  "text-2xl"
    :lg   "text-lg"
    :base "text-base"
    :sm   "text-sm"
    :xs   "text-xs"
    "text-sm"))

(defn- theme->class
  [theme]
  (condp = theme
    :primary   "text-primary"
    :secondary "text-secondary"
    :accent    "text-accent"
    :neutral   "text-neutral"
    :info      "text-info"
    :success   "text-success"
    :warning   "text-warning"
    :error     "text-error"
    "text-neutral"))

(defn- weight->class
  [weight]
  (condp = weight
    :thin       "font-thin"
    :extralight "font-extralight"
    :light      "font-light"
    :normal     "font-normal"
    :medium     "font-medium"
    :semibold   "font-semibold"
    :bold       "font-bold"
    :extrabold  "font-extrabold"
    :black      "font-black"
    "font-normal"))

(defn- color->class
  [color]
  (condp = color
    :red    "text-red-500"
    :green  "text-green-500"
    :blue   "text-blue-500"
    :yellow "text-yellow-500"
    :gray   "text-gray-500"
    :white  "text-white"
    :black  "text-black"
    "text-black"))

(defn ->class
  [classes opts]
  (cond-> classes
    (:center? opts)     (conj "text-center")
    (:italic? opts)     (conj "italic")
    (:underline? opts)  (conj "underline")
    (:uppercase? opts)  (conj "uppercase")
    (:lowercase? opts)  (conj "lowercase")
    (:capitalize? opts) (conj "capitalize")
    (:truncate? opts)   (conj "truncate")
    (:color opts)       (conj (color->class (:color opts)))
    true                (conj (weight->class (:weight opts)))
    true                (conj (size->class (:size opts))) 
    true                (conj (theme->class (:theme opts)))))

(defn base
  ([txt]
   [base {} txt])
  ([{:keys [] :as opts} txt]
   [:span {:class (ut/class-wrapper opts ->class default-classes)}
    txt]))

(defn primary
  ([txt]
   [primary {} txt])
  ([opts txt]
   [base (assoc opts :theme :primary)
    txt]))

(defn error
  ([txt]
   [error {} txt])
  ([opts txt]
   [base (assoc opts :theme :error :color :red)
    txt]))