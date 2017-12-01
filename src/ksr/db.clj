(ns ksr.db
  (:require [korma.db :as kdb]
            [korma.core :as kc]
            [hiccup.util :as hutil]
            [clojure.spec.alpha :as s]
            [ksr.db :as db]
            [clojure.string :as string]))

(kdb/defdb db (kdb/sqlite3 {:db "ksr.db"}))

(defn create-participants! []
  (kc/exec-raw "CREATE TABLE participants(id integer PRIMARY KEY, name text, stand text, einheit text, essen_besonderheiten text, das_letzte_thema_staendekreis text, orden_ist_fuer_mich text, anfahrt text, created DATETIME DEFAULT CURRENT_TIMESTAMP);"))

(defn drop-participants! []
  (kc/exec-raw "DROP TABLE participants;"))

(kc/defentity participants
  (kc/entity-fields :name :einheit :stand :essen_besonderheiten
                    :das_letzte_thema_staendekreis :orden_ist_fuer_mich :anfahrt))

(defn get-participants []
  (for [{:keys [name einheit stand essen_besonderheiten
                das_letzte_thema_staendekreis
                orden_ist_fuer_mich anfahrt]} (kc/select participants)]
    {:name name
     :einheit einheit
     :stand stand
     :essen-besonderheiten essen_besonderheiten
     :das-letzte-thema-staendekreis das_letzte_thema_staendekreis
     :orden-ist-fuer-mich orden_ist_fuer_mich
     :anfahrt anfahrt}))

(defn create-db-if-not-exists! []
  (try
    (get-participants)
    (catch Exception _
      (create-participants!))))

(defn add-participant! [{:keys [name einheit stand essen-besonderheiten
                                das-letzte-thema-staendekreis
                                orden-ist-fuer-mich anfahrt] :as participant}]
  (let [ps (set (db/get-participants))
        _ (create-db-if-not-exists!)]
    (if-not (contains? ps (dissoc participant :__anti-forgery-token))
      (kc/insert
       participants
       (kc/values {:name (hutil/escape-html name)
                   :einheit (hutil/escape-html einheit)
                   :stand (hutil/escape-html stand)
                   :essen_besonderheiten (hutil/escape-html essen-besonderheiten)
                   :das_letzte_thema_staendekreis (hutil/escape-html das-letzte-thema-staendekreis)
                   :orden_ist_fuer_mich (hutil/escape-html orden-ist-fuer-mich)
                   :anfahrt (hutil/escape-html anfahrt)}))
      :duplicate)))

(defn delete-all-participants! []
  (kc/delete participants))

(s/fdef add-participant!
        :args (s/cat :user :ksr.service/user))

(s/fdef delete-all-participants!
        :ret nat-int?)
