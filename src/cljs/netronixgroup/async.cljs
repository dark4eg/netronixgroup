(ns netronixgroup.async
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [timeout]]))


(defn start-loop [fn-loop]
  (go-loop [seconds 1]
           (<! (timeout 1000))
           (fn-loop)
           (recur (inc seconds))))

(defn connectSSE [{:keys [url fn-msg fn-open fn-error fn-loop] :as params}]
  (let [es (js/EventSource. url)]
    (start-loop fn-loop)
    (set! (-> es .-onopen) fn-open)
    (set! (-> es .-onerror) fn-error)
    (.addEventListener es
                       "message"
                       (fn [e]
                         (fn-msg (js->clj (.parse js/JSON (.-data e)) :keywordize-keys true)))
                       false)))