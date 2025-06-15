(ns calorie-calculator.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn load-config []
  (-> "config.edn"
      io/resource
      slurp
      edn/read-string))
