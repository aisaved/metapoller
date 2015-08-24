(ns centipair.core.components.table)



;;table-data
;; :url
;; :page
;; :rows
;; :headers
;; :total

(def per-page 20)


(defn search-list-bar [table-data]
  [:div {:class "action-bar"}
   (if (:search @table-data)
     [:form {:class "form-horizontal"}
     [:div {:class "form-group"}
      [:div {:class "col-sm-4"}
       [:input {:type "search" :placeholder "Search list" :class "form-control"}]]
      [:div {:class "col-sm-1"}
       [:a {:class "btn btn-default"} "Search"]]
      [:div {:class "col-sm-1"}
       [:a {:href (str "#/"
                       (:entity (:create @table-data)) "/create")
            :class "btn btn-primary"} "Create"]]]]
     [:div])])


(defn page-numbers [total]
  (if (> per-page total)
    1
    (if (> (mod total per-page) 0)
      (+ 1 (quot total per-page))
      (quot total per-page))))


(defn page-button [table-data page-count]
  [:li {:class (if (= page-count (:page @table-data)) "active" "")
        :key (str (:id @table-data) "-page-button-list-" page-count)}
   [:a {:href (str "#/" (:url @table-data) "/" (str page-count))
        :key (str (:id @table-data) "-page-button-link-" page-count)} (str page-count)]])


(defn previous-button-disabled? [table-data]
  (<= (:page @table-data) 0))


(defn previous-button [table-data]
  [:li {:class (if (previous-button-disabled? table-data) "disabled" "")}
     [:a {:href (if (previous-button-disabled? table-data) 
                  (str  "#/" (:url @table-data) "/" (str (:page @table-data)))
                  (str  "#/" (:url @table-data) "/" (str (- (:page @table-data) 1))))}
      [:span {:aria-hidden "true"} "<<" ]]])


(defn next-button-disabled? [table-data]
  (>= (:page @table-data) (- (page-numbers (:total @table-data)) 1)))


(defn next-button [table-data]
  [:li {:class (if (next-button-disabled? table-data) "disabled" "")}
     [:a {:href (if (next-button-disabled? table-data)
                  (str "#/"
                       (:url @table-data)
                       "/"
                       (str (:page @table-data)))
                  (str "#/"
                       (:url @table-data)
                       "/"
                       (str (+ (:page @table-data) 1))))}
      [:span {:aria-hidden "true"} ">>" ]]])


(defn data-table-pagination [table-data]
  [:nav {:key (str (:id table-data) "-nav")}
   [:ul {:class "pagination" :key (str (:id table-data) "-pagination-container")}
    (previous-button table-data)
    (doall (map (partial page-button table-data) (range (page-numbers (:total @table-data)))))
    (next-button table-data)]])


(defn data-table [table-data]
  [:div
   (search-list-bar table-data)
   [:table {:class "table table-hover "}
    [:thead (:headers @table-data) ]
    [:tbody (:rows @table-data)]
    ]
   [:div {:key (str (:id @table-data) "-pagination")}
    (data-table-pagination table-data)]])


(defn generate-table-rows [data-list data-container ui-function]
  (if (= (:page data-list) (:page @data-container))
    (let [table-rows (doall (map ui-function (:result data-list)))]
      (swap! data-container assoc :rows table-rows
             :total (:total data-list)))))

