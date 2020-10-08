(ns ksr.models)

(def models
  [{:db/ident :teilnehmer/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Name des Teilnehmenden"}
   {}])