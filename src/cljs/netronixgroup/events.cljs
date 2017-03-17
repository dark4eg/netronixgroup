(ns netronixgroup.events
  (:require [re-frame.core :as re-frame]
            [netronixgroup.db :as db]))

(re-frame/reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(re-frame/reg-event-db
  :source/add
  (fn [db [_ data]]
    (when (not (nil? data))
      (assoc db :source data))))

(re-frame/reg-event-db
  :connection/status
  (fn [db [_ data]]
    (when (not (nil? data))
      (assoc db :connection/status data))))

(re-frame/reg-event-db
  :move/to
  (fn [db [_ {:keys [from to fn] :as data}]]
    (when (not (nil? data))
      (let [new-events (into [] (fn (from db) (to db)))]
        (assoc db from []
                  to new-events
                  (keyword (name to) "count") (count new-events))
        ))))
