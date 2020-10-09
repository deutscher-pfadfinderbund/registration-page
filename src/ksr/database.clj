(ns ksr.database
  (:require [datomic.client.api :as d]
            [ksr.models :as models]
            [ksr.tools :as tools]
            [mount.core :refer [defstate]]))

(def ^:private datomic
  "Configure datomic dev-local."
  {:system "development"
   :server-type :dev-local
   :storage-dir (tools/create-directory! ".datomic/dev-local/data")})

(def ^:private datomic-info
  (atom {:client nil
         :database-name nil}))

(defn- reset-datomic-client!
  "Sets a new datomic client for transactions."
  [datomic-config]
  (swap! datomic-info assoc :client (d/client datomic-config)))

(defn- reset-datomic-db-name!
  "Sets a new database-name for transactions."
  [database-name]
  (swap! datomic-info assoc :database-name database-name))

(defn- create-database!
  "Create a new database. Does not check whether there already is an existing
  database with the same name."
  []
  (let [{:keys [client database-name]} @datomic-info]
    (d/create-database
      client
      {:db-name database-name})))

(defn- new-connection
  "Connects to the database and returns a connection."
  []
  (let [{:keys [client database-name]} @datomic-info]
    (d/connect client {:db-name database-name})))

(defn- create-schema!
  "Creates the schema for discussions inside the database."
  [connection]
  (d/transact connection {:tx-data models/schema}))

(defn transact
  "Shorthand for transaction."
  [data]
  (d/transact (new-connection) {:tx-data data}))


;; -----------------------------------------------------------------------------

(defn init!
  "Initialization function, which does everything needed at a fresh app-install.
  Particularly transacts the database schema defined in models.clj.
  If no parameters are provided, the function reads its configuration from the
  config-namespace."
  ([]
   (init! {:datomic datomic
           :name "development"}))
  ([config]
   (reset-datomic-client! datomic)
   (reset-datomic-db-name! (:name config))
   (when-not (= :peer-server (-> (:datomic config) :server-type))
     (create-database!))
   (create-schema! (new-connection))))

(defstate service
  :start (init!))

;; -----------------------------------------------------------------------------

(def ^:private pfadi-pattern
  [:db/id
   :pfadi/name
   :pfadi/mail
   :pfadi/gruppierung
   :veranstaltung/art])

(defn pfadi-eintragen!
  "Speichert einen Pfadi in die Datenbank."
  [pfadi]
  (transact [pfadi]))

(defn pfadi-verarbeiten
  "Bekommt den Request von der Website und übersetzt das Formular."
  [{:keys [name einheit mail woranteilnehmen]}]
  (let [veranstaltung (case woranteilnehmen
                        "Bundesjungenrat" :veranstaltung.art/bundesjungenrat
                        "Diskussionsrunde" :veranstaltung.art/gesprächsrunde
                        "Beides" :veranstaltung.art/beides)]
    (pfadi-eintragen! {:pfadi/name name
                       :pfadi/gruppierung einheit
                       :pfadi/mail mail
                       :veranstaltung/art veranstaltung})))

(defn alle-pfadis
  "Gibt eine Collection von pfadis zurück."
  []
  (let [pfadis (d/q
                 '[:find (pull ?pfadis pattern) ?when
                   :in $ pattern
                   :where
                   [?pfadis :pfadi/name _ ?tx]
                   [?tx :db/txInstant ?when]]
                 (d/db (new-connection)) pfadi-pattern)
        pfadis' (tools/pull-key-up pfadis :db/ident)]
    (flatten (map (fn [[pfadi timestamp]]
                    (assoc pfadi :erstellt timestamp))
                  pfadis'))))

(comment
  ;; Notwendig zum Starten der Datenbank
  (init!)

  nil)
