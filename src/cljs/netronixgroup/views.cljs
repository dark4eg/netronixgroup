(ns netronixgroup.views
  (:require [re-frame.core :as re-frame]
            [cljsjs.material-ui]
            [cljs-react-material-ui.core :refer [get-mui-theme color]]
            [cljs-react-material-ui.reagent :as ui]
            [cljs-react-material-ui.icons :as ic]
            [reagent.core :as r]
            [cljsjs.leaflet]
            [cljsjs.d3]
            [cljs-time.coerce :as t-coerce]
            [cljs-time.format :as t-format]))

(defn map-component [group]
  (let [event-list (re-frame/subscribe [:event])]
    ;(println 'wtf? group event-list)
    (r/create-class                                         ;; <-- expects a map of functions
      {:component-did-mount
                       #(let [osm-url "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                              osm-attrib "Map data © OpenStreetMap contributors"
                              [lat lng] (->> @event-list
                                             (filter (fn [e] (= group (:name e))))
                                             (last)
                                             :measurements
                                             ;(sort)
                                             (last)
                                             (last))
                              m (doto (js/L.Map. "map")
                                  (.setView (js/L.LatLng. lat lng) 13)
                                  (.addLayer (js/L.TileLayer. osm-url
                                                              #js {:minZoom     1
                                                                   :maxZoom     19
                                                                   :attribution ""})))
                              svg (.. js/d3
                                      (select (.-overlayPane (.getPanes m)))
                                      (append "svg")
                                      ;(attr "width" width)
                                      ;(attr "height" height)
                                      )
                              g (.. svg
                                    (append "g")
                                    (attr "class" "leaflet-zoom-hide")
                                    )
                              projectPoint (fn [x y]
                                             (let [p (.latLngToLayerPoint m (js/L.LatLng. x y))]
                                               (.point js-this (.-x p) (.-y p))))
                              applyLatLngLayer (fn [d]
                                                 (let [y (nth (-> d .-geometry .-coordinates) 1)
                                                       x (-> d .-geometry .-coordinates)]
                                                   ))
                              marker (.. g
                                         (append "circle")
                                         (attr "id" "marker")
                                         (attr "class" "travel-marker"))
                              reset (fn [e]
                                      (let []
                                        (.attr marker "transform")))]
                          )
       ;:component-should-update #(false)

       :component-will-mount
                       #()
       :display-name   "map-component"
       :reagent-render (fn [x y z]
                         [:div#map-container {:key   :map-container
                                              :style {:position "relative"
                                                      :width    "100%"
                                                      :height   "100%"}}
                          [:div#map {:key   :map
                                     :style {:position "relative"
                                             :width    "100%"
                                             :height   "100%"}}]])})))

(defn simple-list [group]
  (let [event-list (re-frame/subscribe [:event])]
    (fn []
      [ui/list
       (for [[i v] (map vector (range) (->> @event-list
                                            (filter (fn [e] (= group (:name e))))
                                            (map #(let [m (:measurements %)
                                                        unit (:unit %)]
                                                    (when (vector? m)
                                                      (map (fn [e]
                                                             {:time  (nth e 0)
                                                              :value (nth e 1)
                                                              :unit unit})
                                                           m))))
                                            (flatten)
                                            (sort-by :time)
                                            (reverse)
                                            (take 10)
                                            ))]
         [ui/list-item {:key (->> (str group i)
                                  (filter #(not (nil? %))))
                        :primaryText (str (:value v) " " (:unit v))
                        :secondaryText (->> (:time v)
                                            (* 1000)
                                            (t-coerce/from-long)
                                            (t-format/unparse
                                              (t-format/formatter "yyyy-MM-dd HH:mm:ss")))}])])))

(defn main-panel []
  (let [name (re-frame/subscribe [:name])
        connect (re-frame/subscribe [:connection/status])
        event-list (re-frame/subscribe [:event])
        event-count (re-frame/subscribe [:event/count])]
    (fn []
      [ui/mui-theme-provider
       {:mui-theme (get-mui-theme
                     {:palette {:text-color (color :cyan500)
                                :primary-color (color :cyan500)
                                :disabled-color (color :grey100)}})}
       [:div
        [ui/app-bar {:title                 @name
                     :show-menu-icon-button false}]

        [:div {:className "fluid-container"}
         [:div {:className "row center-xs"}
          [:div {:className "col-xs-12"
                 :style     {:paddingTop    "20px"
                             :paddingBottom "10px"
                             :fontSize      "16px"}}
           "All received events "
           [:b @event-count]]]
         [:div {:className "row"}
          (for [[group events] (->> @event-list
                                    (sort-by :name)
                                    (group-by :name))]
            [:div {:key       (str "group-" group)
                   :className "col-xs-12 col-sm-6 col-md-6 col-lg-4"
                   :style     {:paddingBottom "15px"}}
             [ui/paper {:z-depth 4}
              [:div {:className "row"}
               [:div {:className "col-xs-12"}
                [ui/flat-button {:label    (str group " events " (count events))
                                 :disabled true}]]
               [:div {:className "col-xs-12"
                      :style     (if (= group "Location")
                                   {:height   "736px"
                                    :paddingLeft  0
                                    :paddingRight 0}
                                   {:min-height "300px"})}
                (case group
                  "Location" [map-component group]
                  [simple-list group])]]

              ]])]]]])))
