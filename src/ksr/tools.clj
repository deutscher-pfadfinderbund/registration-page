(ns ksr.tools
  (:require [clojure.walk :as walk])
  (:import (clojure.lang PersistentArrayMap)
           (java.io File)))

(defn create-directory!
  "Creates a directory in the project's path. Returns the absolut path of the
  directory."
  [^String path]
  (when-not (or (.startsWith path "/")
                (.startsWith path ".."))
    (let [dir (File. path)]
      (.mkdirs dir)
      (.getAbsolutePath dir))))

(defn pull-key-up
  "Schaut in einer verschachtelten Struktur nach dem `key-name` und zieht ihn
  eine Ebene nach oben."
  [coll key-name]
  (walk/postwalk
    #(if (and (= PersistentArrayMap (type %)) (contains? % key-name))
       (key-name %)
       %)
    coll))