(ns calorie-calculator.services
  (:require [clj-http.client :as client]
            [calorie-calculator.config :refer [load-config]])
  (:import (clojure.lang ExceptionInfo))) ; <-- IMPORTAÇÃO ADICIONADA AQUI

;; Carrega hosts e chaves do config.edn
(let [{:keys [rapi-host rapi-key ninjas-host ninjas-key]} (load-config)]
  (def food-url      "https://dietagram.p.rapidapi.com/apiFood.php")
  (def activity-url  "https://calories-burned-by-api-ninjas.p.rapidapi.com/v1/caloriesburned")
  (def rapi-headers  {"x-rapidapi-host" rapi-host
                      "x-rapidapi-key"  rapi-key})
  (def ninjas-headers {"x-rapidapi-host" ninjas-host
                       "x-rapidapi-key"  ninjas-key}))

(defn safe-get [url opts]
  (try
    (client/get url opts)
    (catch ExceptionInfo ex
      (let [{:keys [status body]} (ex-data ex)]
        (println (format "Erro HTTP %s: %s" status body))
        nil))))

(defn fetch-food-calories
  "Busca calorias de um alimento pelo nome e retorna a caloria (Integer)."
  [food-name]
  (if-let [resp (safe-get food-url
                          {:headers      rapi-headers
                           :query-params {:name food-name
                                          :lang "pt"}
                           :as           :json})]
    (let [dishes      (get-in resp [:body :dishes])
          first-dish  (first dishes)
          caloric-str (get first-dish :caloric "0")
          caloric-int (try (Integer/parseInt caloric-str)
                           (catch Exception _ 0))]
      caloric-int)
    (do
      (println "  Falha ao obter calorias; retornando 0.")
      0)))


(defn fetch-activity-calories
  "Busca calorias queimadas por atividade (nome) e duração em minutos."
  [activity duration]
  (if-let [resp (safe-get activity-url
                          {:headers      ninjas-headers
                           :query-params {:activity activity}
                           :as           :json})]
    (let [rate (get-in resp [:body 0 :calories_per_minute] 0)]
      (int (* rate duration))
    (do
      (println "  Falha ao obter calorias da atividade; retornando 0.")
      0))))
