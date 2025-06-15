(ns calorie-calculator.core
  (:gen-class)
  (:require [clj-http.client     :as client]
            [cheshire.core       :as json]
            [calorie-calculator.config :refer [load-config]]
            [clojure.string       :as str]))

(defn prompt [msg]
  (println msg)
  (print "> ")
  (flush)
  (read-line))

(defn parse-int [s]
  (try (Integer/parseInt s)
       (catch Exception _ 0)))

(defn parse-float [s]
  (try (Double/parseDouble s)
       (catch Exception _ 0.0)))

(defn register-user-cli []
  (let [user {:nome    (prompt "Nome:")
              :idade   (parse-int  (prompt "Idade:"))
              :sexo    (keyword (str/upper-case (prompt "Sexo (M/F):")))
              :peso    (parse-float (prompt "Peso (kg):"))
              :altura  (parse-float (prompt "Altura (m):"))}
        resp (client/post "http://localhost:3000/user"
                          {:headers {"Content-Type" "application/json"}
                           :body    (json/generate-string user)
                           :as      :json})]
    (println "\n✔ Usuário cadastrado:" (:body resp))))

(defn add-food-cli []
  (let [payload {:descricao (prompt "Nome do alimento:")
                 :date      (prompt "Data (YYYY-MM-DD):")
                 :qty       (parse-int (prompt "Quantidade:"))}
        resp    (client/post "http://localhost:3000/food"
                             {:headers {"Content-Type" "application/json"}
                              :body    (json/generate-string payload)
                              :as      :json})]
    (println "\n✔ Alimento registrado:" (:body resp))))

(defn add-activity-cli []
  (let [payload {:descricao (prompt "Atividade física:")
                 :date      (prompt "Data (YYYY-MM-DD):")
                 :duracao   (parse-int (prompt "Duração (min):"))}
        resp    (client/post "http://localhost:3000/activity"
                             {:headers {"Content-Type" "application/json"}
                              :body    (json/generate-string payload)
                              :as      :json})]
    (println "\n✔ Atividade registrada:" (:body resp))))

(defn show-extract-cli []
  (let [from (prompt "Extrato de (YYYY-MM-DD):")
        to   (prompt "Até (YYYY-MM-DD):")
        resp (client/get "http://localhost:3000/extract"
                         {:query-params {"from" from "to" to}
                          :as           :json})
        entries (:body resp)]
    (println "\n--- Extrato de Transações ---")
    (if (seq entries)
      (doseq [e entries] (println e))
      (println "Nenhuma transação no período."))
    (println "-----------------------------")))

(defn show-balance-cli []
  (let [from (prompt "Saldo de (YYYY-MM-DD):")
        to   (prompt "Até (YYYY-MM-DD):")
        resp (client/get "http://localhost:3000/balance"
                         {:query-params {"from" from "to" to}
                          :as           :json})
        bal  (get-in resp [:body :balance])]
    (println (format "\nSaldo de calorias entre %s e %s: %d" from to bal))))

(defn menu []
  (println "\n=== Calculadora de Calorias ===")
  (println "1) Cadastrar usuário")
  (println "2) Registrar alimento")
  (println "3) Registrar atividade")
  (println "4) Ver extrato")
  (println "5) Ver saldo")
  (println "0) Sair"))

(defn -main [& args]
  (load-config)
  (println "CLI iniciado. Conectando-se à API em http://localhost:3000\n")
  (loop []
    (menu)
    (case (str/trim (prompt "Escolha uma opção"))
      "1" (register-user-cli)
      "2" (add-food-cli)
      "3" (add-activity-cli)
      "4" (show-extract-cli)
      "5" (show-balance-cli)
      "0" (do (println "Até logo!") (System/exit 0))
      (println "Opção inválida!"))
    (recur)))
