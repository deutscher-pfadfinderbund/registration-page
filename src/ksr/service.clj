(ns ksr.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.csrf :as csrf]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]
            [hiccup.page :as hp]
            [clojure.spec.alpha :as s]
            [geheimtur.interceptor :as gti]
            [ksr.database :as database]))

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
       (hp/include-css "css/main.css")
       (hp/include-js "js/modernizr-custom.js")]
      [:body
       [:div.container.pb-5
        [:div.row
         [:div.offset-1.offset-md-1.col-10.col-md-10.col-sm-12.card.card-3
          [:div.text-center.pb-5
           [:img.w-75 {:src "img/schriftzug-dpb.svg"}]]
          [:h1.text-center.pb-5 "Jungenbundführerlager 2020"]
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
   [:h3 "Organisation"]
   [:div.row.d-flex.justify-content-center
    [:div.col-4.col-md-2
     [:img.card-img.py-2
      {:src "/img/Hohenstaufen_mini.svg"
       :style {:max-width "300px"}}]]
    [:div.col-12.col-md-10
     [:p "Organisiert wird das digitale Lager von der Jungenbundführung und von eurer Lieblingsjungenschaft, der
     Jungenschaft Hohenstaufen."]
     [:p "Bitte nehmt uns ein wenig Arbeit ab und meldet euch über das Formular
     an. Zur besseren Einschätzung der Teilnehmer finden wir die Einzelanmeldung
     sinnvoll."]
     [:p "Sollte es Probleme geben, wovon wir mal nicht ausgehen, könnt ihr euch
     bei rambo melden: "
      [:a {:href "mailto:vogt@jungenbund.de"} "vogt@jungenbund.de"] "."]
     [:p "Wir freuen uns auf euch!"]]]

   footer-nav])


;; -----------------------------------------------------------------------------

(defn csrf-form-input [request]
  [:input {:name "__anti-forgery-token" :value (::csrf/anti-forgery-token request) :type "hidden"}])

(defn home-page [request]
  (with-header
    [:div.row
     [:div.col-12.col-md-5
      [:h3 "Einladung"]
      [:div.card.card-1
       [:a {:href "pdf/einladung.pdf"
            :target "_blank"}
        [:picture.hover.img-fluid
         [:source {:srcset "pdf/einladung.webp" :type "image/webp"}]
         [:img {:src "pdf/einladung.png"}]]]]]
     [:div.col-12.offset-md-1.col-md-6
      [:h3 "Eckdaten"]
      [:h5 "Wann?"] [:p "7. November 2020, 10 Uhr"]
      [:h5 "Wo?"] [:p "Digital, wir verwenden BigBlueButton. Link kommt per Mail."]
      [:h5 "Sollte ich teilnehmen?"] [:p "Ja"]]]

    [:h2.pt-5 "Anmeldung"]
    [:p "Melde dich hier an für das digitale Jungenbundführerlager! Wir benötigen die Daten, um dir die Zugangsdaten und
    weitere Informationen schicken können."]
    [:form {:action "/registrieren" :method :POST}
     [:div.form-group
      [:label#name "Name *"]
      [:input.form-control {:name "name" :required true}]]
     (csrf-form-input request)
     [:div.form-group
      [:label "Einheit / Gruppierung *"]
      [:input.form-control {:name "einheit" :required true}]]
     [:div.form-group
      [:label "E-Mail *"]
      [:input.form-control {:name "mail" :required true :type "email"}]]
     [:div.form-group
      [:label "Ich möchte an folgendem teilnehmen *"]
      [:select.form-control {:name "woranteilnehmen" :required true}
       [:option {:value "Bundesjungenrat"} "Bundesjungenrat"]
       [:option {:value "Diskussionsrunde"} "Diskussionsrunde am Nachmittag"]
       [:option {:value "Beides"} "Bundesjungenrat und Diskussionsrunde"]]]
     [:input {:class "btn btn-primary"
              :type :submit
              :value "Anmelden"}]]
    footer))

(defn register-page [{{:keys [name]} :form-params :as request}]
  (database/pfadi-verarbeiten (:form-params request))
  (with-header
    [:div
     [:div.alert.alert-success {:style {:margin "1rem 0"}}
      "Du hast dich erfolgreich zum Lager angemeldet!"]
     [:p "Nun steht dein Name auf unserer Liste, " name ". Wir freuen uns schon auf dich!"]
     [:p "Gehe hier " [:a {:href "/"} "zurück"] ", um dir die anderen Informationen durchzulesen oder um eine weitere Person anzumelden."]]))


;; -----------------------------------------------------------------------------
;; Admin

(defn admin-page [_request]
  (with-header
    [:section
     (let [pfadis (database/alle-pfadis)]
       [:table.table.table-striped.table-hover
        [:thead
         [:tr
          [:th "Name"]
          [:th "Gruppierung"]
          [:th "Mail"]
          [:th "Veranstaltung"]
          [:th "Angemeldet am"]]]
        [:tbody
         (for [pfadi pfadis]
           [:tr
            [:td (:pfadi/name pfadi)]
            [:td (:pfadi/gruppierung pfadi)]
            [:td (:pfadi/mail pfadi)]
            [:td (:veranstaltung/art pfadi)]
            [:td (:erstellt pfadi)]])]])]
    (hp/include-css "node_modules/datatables/media/css/jquery.dataTables.min.css")
    (hp/include-js "node_modules/jquery/dist/jquery.min.js")
    (hp/include-js "node_modules/datatables/media/js/jquery.dataTables.min.js")
    [:script "
    $(document).ready(function() {
        $('table').DataTable();
    });
    "]))


;; -----------------------------------------------------------------------------

(def users
  (let [user (or (System/getenv "ADMIN_USER") "ksr")
        pass (or (System/getenv "ADMIN_PASSWORD") "diskette")]
    {user {:password pass
           :roles #{:admin}}}))

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
                                     [(gti/guard :silent? false :roles #{:admin}) `admin-page])]})

(def ^:private port
  (let [from-env (System/getenv "PORT")]
    (if from-env
      (Integer/parseInt from-env)
      8080)))

(def service {:env :prod
              ::http/enable-csrf {}
              ::http/routes routes
              ::http/resource-path "/public"
              ::http/type :jetty
              ::http/host "0.0.0.0"
              ::http/port port
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
