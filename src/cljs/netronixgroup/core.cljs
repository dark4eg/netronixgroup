(ns netronixgroup.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [re-frisk.core :refer [enable-re-frisk!]]
            [netronixgroup.events]
            [netronixgroup.subs]
            [netronixgroup.views :as views]
            [netronixgroup.config :as config]
            [netronixgroup.async :as async]))

(defn on-window-resize [evt]
  (re-frame/dispatch-sync [:window/size {:width  (.-innerWidth js/window)
                                         :height (.-innerHeight js/window)}]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (enable-re-frisk!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn initEventSourcing []
  (async/connectSSE {:url      config/eventEndPoint
                     :fn-msg   (fn [e]
                                 (re-frame/dispatch-sync [:source/add e]))
                     :fn-open  (fn [e]
                                 (re-frame/dispatch-sync [:connection/status :connected]))
                     :fn-error (fn [e]
                                 (println 'error )
                                 (re-frame/dispatch-sync [:connection/status (if (= 0 (.-readyState (js-this)))
                                                                               :reconnecting
                                                                               :disconnected)]))
                     :fn-loop (fn []
                                (re-frame/dispatch-sync [:move/to {:from :source
                                                                   :to :event
                                                                   :fn concat}]))}))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db])
  (dev-setup)
  (mount-root)
  (initEventSourcing)
  ;(.addEventListener js/window "resize" on-window-resize)
  )
