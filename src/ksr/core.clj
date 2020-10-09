(ns ksr.core
  (:gen-class)
  (:require [ksr.mount]))

(defn -main [& _args]
  (ksr.mount/start))