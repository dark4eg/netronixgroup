(ns netronixgroup.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
  :event
  (fn [db]
    (:event db)))

(re-frame/reg-sub
  :event/by-name
  (fn [db [_ name]]
    (reaction (vals (:event @db)))))

(re-frame/reg-sub
  :event/count
  (fn [db]
    (:event/count db)))

(re-frame/reg-sub
  :connection/status
  (fn [db]
    (:connection/status db)))
