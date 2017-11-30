(ns ksr.db-test
  (:require [ksr.db :as db]
            [clojure.test :refer [deftest is]]))

(def participant
  {:name "kangaroo"
   :einheit "Das asoziale Netzwerk"
   :stand "Nüscht"
   :essen-besonderheiten "Nein"
   :das-letzte-thema-staendekreis "Windows Vista installieren"
   :orden-ist-fuer-mich "..."})

(deftest add-and-query-participant
  (let [res (db/add-participant! participant)]
    (is (pos? ((ffirst res) res)))
    (is (not (empty? (db/get-participants))))
    (is (pos? (db/delete-all-participants!)))))
