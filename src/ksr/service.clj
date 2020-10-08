(ns ksr.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.csrf :as csrf]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]
            [hiccup.page :as hp]
            [clojure.spec.alpha :as s]
            [geheimtur.interceptor :as gti]
            [ksr.db :as db]))

(defn with-header [& body]
  (ring-resp/response
    (hp/html5
      [:head
       [:title "Jungenbundführerlager 2020"]
       [:meta {:charset "utf-8"}]
       [:meta {:http-equiv "X-UA-Compatible"
               :content "IE=edge"}]
       [:meta {:name "viewport"
               :content "width=device-width, initial-scale=1"}]
       (hp/include-css "css/main.css")]
      [:body
       [:div.container.pb-5
        [:div.row
         [:div.offset-1.offset-md-1.col-10.col-md-10.col-sm-12.card.card-3
          [:div.text-center.pb-5
           [:img.w-75 {:src "img/schriftzug-dpb.svg"}]]
          [:h1.text-center "Jungenbundführerlager 2020"]
          body]]]])))

(def footer-nav
  [:section
   [:hr {:style {:margin "1rem"}}]
   [:div.text-right
    [:span [:a.btn.btn-link {:href "https://deutscher-pfadfinderbund.de/impressum/"} "Impressum und Datenschutz"]]
    [:span [:a.btn.btn-link {:href "/anmeldungen"} "Anmeldungen"]]]])

(def footer
  [:footer
   [:hr {:style {:margin-top "3rem"}}]
   [:h2 "Informationen"]
   [:div.row
    [:div.col-12.col-md-5
     [:h3 "Einladung"]
     [:div.card.card-1
      [:a {:href "pdf/einladung-ksr.pdf"
           :target "_blank"}
       [:img.hover.img-fluid
        {:src "pdf/einladung-ksr.png"
         :style {:width "300px"}}]]]]
    [:div.col-md-1]
    [:div.col-12.col-md-6
     [:h3 "Eckdaten"]
     [:h5 "Wann?"] [:p "27.04. - 29.04.2018"]
     [:h5 "Sollte ich teilnehmen?"] [:p "Ja"]]]
   [:br]
   [:h3 "Organisation"]
   [:div.row
    [:div.col-12.col-md-4
     [:div.card.card-1
      [:img.img-fluid
       {:src "img/jhs.gif"
        :style {:max-width "300px"}}]]]
    [:div.col-12.col-md-6
     [:p "Organisiert wird das digitale Lager von eurer Lieblingsjungenschaft, der
     Jungenschaft Hohenstaufen."]
     [:p "Bitte nehmt uns ein wenig Arbeit ab und meldet euch über das Formular
     an. Zur besseren Einschätzung der Teilnehmer finden wir die Einzelanmeldung
     sinnvoll."]
     [:p "Sollte es Probleme geben, wovon wir mal nicht ausgehen, könnt ihr euch
     bei rambo melden: "
      [:a {:href "vogt@jungenbund.de"} "vogt@jungenbund.de"] "."]
     [:p "Wir freuen uns auf euch!"]]]

   footer-nav])


;; -----------------------------------------------------------------------------

(defn csrf-form-input [request]
  [:input {:name "__anti-forgery-token" :value (::csrf/anti-forgery-token request) :type "hidden"}])

(defn home-page [request]
  (with-header

    footer))

(defn register-page [{{:keys [name]} :form-params :as request}]
  (let [status (db/add-participant! (:form-params request))]
    (with-header
      (if-not (= :duplicate status)
        [:div
         [:div.alert.alert-success {:style {:margin "1rem 0"}}
          "Du hast dich erfolgreich zum Lager angemeldet!"]
         [:p "Nun steht dein Name auf unserer Liste, " name ". Wir freuen uns schon auf dich!"]
         [:p "Gehe hier " [:a {:href "/"} "zurück"] ", um dir die anderen Informationen durchzulesen oder um eine weitere Person anzumelden."]]
        [:div
         [:div.alert.alert-danger
          "Der Datensatz, den du eintragen wolltest, existiert schon in unserer
          Datenbank. Vielleicht hast du versucht dich doppelt anzumelden... Bei
          Problemen kontaktiere rambo unter dieser Adresse: "
          [:a {:href "ksr@dpb-remscheid.de"} "ksr@dpb-remscheid.de"]]
         [:p "Gehe hier " [:a {:href "/"} "zurück"] " und probiere es erneut."]]))))


;; -----------------------------------------------------------------------------
;; Admin

(defn- users->rows []
  (for [{:keys [name einheit stand]} (db/get-participants)
        :when (not (nil? name))]
    [:tr [:td name] [:td einheit] [:td stand]]))

(defn- extract-data [db k]
  (for [t (sort (remove empty? (map k db)))]
    [:div.alert.alert-secondary t]))

(defn- get-by-stand [db stand]
  [:li (str stand ": ") (count (filter #(= stand %) (map :stand db)))])

(defn admin-page [request]
  (with-header
    (let [db (db/get-participants)]
      [:div
       [:h2 "Anmeldungen"]

       [:ul
        [:li [:strong "Anmeldungen Gesamt: " (count db)]]
        (get-by-stand db "Jungwolf")
        (get-by-stand db "Knappe")
        (get-by-stand db "Späher")
        (get-by-stand db "St.-Georgs-Knappe")
        (get-by-stand db "Ordensritter")
        (get-by-stand db "St.-Georgs-Ritter")
        (get-by-stand db "Sonstiges")]

       [:table.table.table-striped
        [:thead [:tr [:th "Name"] [:th "Einheit"] [:th "Stand"]]]
        [:tbody (users->rows)]]

       [:br]
       [:h4 "Die letzten Themen der Ständekreise"]
       (extract-data db :das-letzte-thema-staendekreis)

       [:br]
       [:h4 "Orden ist für mich..."]
       (extract-data db :orden-ist-fuer-mich)

       [:br]
       [:h4 "Sonstige Informationen"]
       (extract-data db :anfahrt)])))


;; -----------------------------------------------------------------------------

(def users
  (let [user (or (System/getenv "KSR_USER") "ksr")
        pass (or (System/getenv "KSR_PASS") "diskette")]
    {user {:password pass
           :roles #{(keyword user)}
           :full-name "Knäppchen"}}))

(defn credentials
  [_ {:keys [username password]}]
  (when-let [identity (get users username)]
    (when (= password (:password identity))
      (dissoc identity :password))))

(def common-interceptors [(body-params/body-params) http/html-body (csrf/anti-forgery)])
(def http-basic-interceptors (into common-interceptors [(gti/http-basic "... Nope." credentials)]))

(def routes #{["/" :get (conj common-interceptors `home-page)]
              ["/registrieren" :post (conj common-interceptors `register-page)]
              ["/anmeldungen" :get (into http-basic-interceptors
                                         [(gti/guard :silent? false :roles #{:ksr}) `admin-page])]})

(def service {:env :prod
              ::http/enable-csrf {}
              ::http/routes routes
              ::http/resource-path "/public"
              ::http/type :jetty
              ::http/port 8080
              ::http/secure-headers {:content-security-policy-settings {:object-src "none"}}
              ::http/container-options {:h2c? true
                                        :h2? false
                                        :ssl? false}})

;; -----------------------------------------------------------------------------

(s/def ::maybe-string (s/nilable string?))
(s/def ::name ::maybe-string)
(s/def ::einheit ::maybe-string)
(s/def ::essen-besonderheiten ::maybe-string)
(s/def ::das-letzte-thema-staendekreis ::maybe-string)
(s/def ::orden-ist-fuer-mich ::maybe-string)
(s/def ::anfahrt ::maybe-string)
(s/def ::user
  (s/keys :req-un [::name ::einheit ::essen-besonderheiten ::anfahrt
                   ::das-letzte-thema-staendekreis ::orden-ist-fuer-mich]))
