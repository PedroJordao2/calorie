(ns calorie-calculator.db)

(def state
  (atom {:user    nil
         :entries []}))

(defn set-user! [user]
  (swap! state assoc :user user))

(defn add-entry! [entry]
  (swap! state update :entries conj entry))

(defn get-entries [from to]
  ;; Garante que 'start' ≤ 'end', mesmo se o usuário inverter as datas
  (let [[start end] (if (neg? (compare from to))
                      [from to]
                      [to from])]
    (filter (fn [{:keys [date]}]
              ;; date >= start  &&  date <= end
              (and (>= (compare date start) 0)
                   (<= (compare date end)   0)))
            (:entries @state))))

(defn balance [from to]
  (->> (get-entries from to)
       (reduce (fn [acc {:keys [tipo calorias]}]
                 (if (= tipo :ganho)
                   (+ acc calorias)
                   (- acc calorias)))
               0)))
