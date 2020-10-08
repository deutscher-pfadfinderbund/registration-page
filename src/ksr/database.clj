(ns ksr.database
  (:require [datomic.client.api :as d])
  (:import (java.io File)))

(defn- create-directory!
  "Creates a directory in the project's path. Returns the absolut path of the
  directory."
  [^String path]
  (when-not (or (.startsWith path "/")
                (.startsWith path ".."))
    (let [dir (File. path)]
      (.mkdirs dir)
      (.getAbsolutePath dir))))

(def ^:private datomic
  "Configure datomic dev-local."
  {:system "development"
   :server-type :dev-local
   :storage-dir (create-directory! ".datomic/dev-local/data")})

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
  (d/transact connection {:tx-data models/datomic-schema}))

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