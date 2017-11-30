(ns ksr.db
  (:require [korma.db :as kdb]
            [korma.core :as kc]
            [clojure.spec.alpha :as s]))

(kdb/defdb db (kdb/sqlite3 {:db "ksr.db"}))

(defn create-participants! []
  (kc/exec-raw "CREATE TABLE participants(id integer PRIMARY KEY, name text, stand text, einheit text, essen_besonderheiten text, das_letzte_thema_staendekreis text, orden_ist_fuer_mich text, created DATETIME DEFAULT CURRENT_TIMESTAMP);"))

(defn drop-participants! []
  (kc/exec-raw "DROP TABLE participants;"))

(kc/defentity participants
  (kc/entity-fields :name :einheit :stand :essen_besonderheiten
                    :das_letzte_thema_staendekreis :orden_ist_fuer_mich))

(defn get-participants []
  (for [{:keys [name einheit stand essen_besonderheiten
                das_letzte_thema_staendekreis
                orden_ist_fuer_mich]} (kc/select participants)]
    {:name name
     :einheit einheit
     :stand stand
     :essen-besonderheiten essen_besonderheiten
     :das-letzte-thema-staendekreis das_letzte_thema_staendekreis
     :orden-ist-fuer-mich orden_ist_fuer_mich}))

(defn create-db-if-not-exists! []
  (try
    (get-participants)
    (catch Exception _
      (create-participants!))))

(defn add-participant! [{:keys [name einheit stand essen-besonderheiten
                                das-letzte-thema-staendekreis
                                orden-ist-fuer-mich]}]
  (create-db-if-not-exists!)
  (kc/insert
   participants
   (kc/values {:name name
               :einheit einheit
               :stand stand
               :essen_besonderheiten essen-besonderheiten
               :das_letzte_thema_staendekreis das-letzte-thema-staendekreis
               :orden_ist_fuer_mich orden-ist-fuer-mich})))

(defn delete-all-participants! []
  (kc/delete participants))

(s/fdef add-participant!
        :args (s/cat :user :ksr.service/user))

(s/fdef delete-all-participants!
        :ret nat-int?)
