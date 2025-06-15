(ns calorie-calculator.handler
  (:require [compojure.core :refer [defroutes GET POST context]]
            [compojure.route :refer [not-found]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [response]]
            [hiccup.page :refer [html5]]
            [hiccup.form :refer [form-to label text-field submit-button]]
            [ring.adapter.jetty :refer [run-jetty]]
            [calorie-calculator.db :as db]
            [calorie-calculator.services :as svc])
  (:gen-class))

(defn layout [title & body]
  (html5
    [:head
     [:meta {:charset "UTF-8"}]
     [:meta {:name "viewport" :content "width=device-width,initial-scale=1"}]
     [:title title]]
    [:body body]))

(defn index-page []
  (layout "Calculadora de Calorias"
    [:h1 "Calculadora de Calorias"]
    [:ul
     [:li [:a {:href "/user-form"} "Cadastrar Usuário"]]
     [:li [:a {:href "/food-form"} "Registrar Alimento"]]
     [:li [:a {:href "/activity-form"} "Registrar Atividade"]]
     [:li [:a {:href "/extract-form"} "Ver Extrato"]]
     [:li [:a {:href "/balance-form"} "Ver Saldo"]]]))

(defn user-form-page []
  (layout "Cadastro de Usuário"
    [:h2 "Cadastrar Usuário"]
    (form-to [:post "/user"]
      (label "nome" "Nome:") (text-field "nome") [:br]
      (label "idade" "Idade:") (text-field {:type "number"} "idade") [:br]
      (label "sexo" "Sexo (M/F):") (text-field "sexo") [:br]
      (label "peso" "Peso (kg):") (text-field {:type "number"} "peso") [:br]
      (label "altura" "Altura (m):") (text-field {:type "number"} "altura") [:br]
      (submit-button "Cadastrar"))))

(defn food-form-page []
  (layout "Registrar Alimento"
    [:h2 "Registrar Alimento"]
    (form-to [:post "/food"]
      (label "descricao" "Descrição:") (text-field "descricao") [:br]
      (label "date" "Data (YYYY-MM-DD):") (text-field {:type "date"} "date") [:br]
      (label "qty" "Quantidade:") (text-field {:type "number"} "qty") [:br]
      (submit-button "Registrar"))))

(defn activity-form-page []
  (layout "Registrar Atividade"
    [:h2 "Registrar Atividade"]
    (form-to [:post "/activity"]
      (label "descricao" "Descrição:") (text-field "descricao") [:br]
      (label "date" "Data (YYYY-MM-DD):") (text-field {:type "date"} "date") [:br]
      (label "duracao" "Duração (min):") (text-field {:type "number"} "duracao") [:br]
      (submit-button "Registrar"))))

(defn extract-form-page []
  (layout "Ver Extrato"
    [:h2 "Extrato"]
    (form-to [:get "/extract"]
      (label "from" "De:") (text-field {:type "date"} "from") [:br]
      (label "to" "Até:") (text-field {:type "date"} "to") [:br]
      (submit-button "Buscar Extrato"))))

(defn balance-form-page []
  (layout "Ver Saldo"
    [:h2 "Saldo de Calorias"]
    (form-to [:get "/balance"]
      (label "from" "De:") (text-field {:type "date"} "from") [:br]
      (label "to" "Até:") (text-field {:type "date"} "to") [:br]
      (submit-button "Buscar Saldo"))))

(defroutes api-routes
  (POST "/user" req
    (db/set-user! (:body req))
    {:status 200 :body {:msg "Usuário cadastrado"}})

  (POST "/food" req
    (let [{:keys [descricao date qty]} (:body req)
          cals (* (svc/fetch-food-calories descricao) qty)
          entry {:tipo :ganho :descricao descricao :date date :calorias cals}]
      (db/add-entry! entry)
      {:status 200 :body entry}))

  (POST "/activity" req
    (let [{:keys [descricao date duracao]} (:body req)
          cals (svc/fetch-activity-calories descricao duracao)
          entry {:tipo :perda :descricao descricao :date date :calorias cals}]
      (db/add-entry! entry)
      {:status 200 :body entry}))

  (GET "/extract" [from to]
    {:status 200 :body (db/get-entries from to)})

  (GET "/balance" [from to]
    {:status 200 :body {:balance (db/balance from to)}}))

(defroutes ui-routes
  (GET "/" [] (response (index-page)))
  (GET "/user-form" [] (response (user-form-page)))
  (GET "/food-form" [] (response (food-form-page)))
  (GET "/activity-form" [] (response (activity-form-page)))
  (GET "/extract-form" [] (response (extract-form-page)))
  (GET "/balance-form" [] (response (balance-form-page)))
  (not-found "Página não encontrada"))

(def app
  (-> (context "/" [] api-routes ui-routes)
      wrap-params
      (wrap-json-body {:keywords? true})
      wrap-json-response))

(defn -main [& args]
  (println "Servidor de API iniciado em http://localhost:3000")
  (run-jetty app {:port 3000}))
