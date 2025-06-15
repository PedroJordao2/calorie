(defproject calorie-calculator "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring "1.8.2"]                     ;; Compatível com Java 8
                 [ring/ring-jetty-adapter "1.8.2"] ;; Jetty antigo compatível
                 [compojure "1.7.0"]
                 [hiccup "1.0.5"]
                 [ring/ring-json "0.5.1"]
                 [clj-http "3.12.3"]
                 [cheshire "5.11.0"]]
  :main calorie-calculator.handler)
