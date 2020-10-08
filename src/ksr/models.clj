(ns ksr.models)

(def schema
  [{:db/ident :pfadi/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Name des Teilnehmenden"}
   {:db/ident :pfadi/gruppierung
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Gruppierung"}
   {:db/ident :pfadi/mail
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "E-Mail"}
   {:db/ident :veranstaltung.art/bundesjungenrat}
   {:db/ident :veranstaltung.art/gesprächsrunde}
   {:db/ident :veranstaltung.art/beides}
   {:db/ident :veranstaltung/art
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "Woran soll teilgenommen werden?"}])
