(ns centipair.core.utilities.pagination)



(defn offset-limit [page per]
  (let [page-number (if (nil? page) 0 (Integer. page))
        per-page (if (nil? per) 50 (Integer. per))]
    {:offset (* per-page page-number)
     :limit per-page}))
