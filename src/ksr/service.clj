(ns ksr.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]
            [hiccup.page :as hp]))

(def state (atom {:teilnehmer []}))

(defn with-header [& body]
  (ring-resp/response
   (hp/html5
    (hp/include-css "css/bootstrap.min.css")
    (hp/include-css "css/main.css")
    (hp/include-js "https://www.google.com/recaptcha/api.js")
    [:div.container
     [:div.row
      [:div.col-md-1]
      [:div.col-md-10.col-sm-10.card.card-3
       [:div.row
        [:div.col-1]
        [:div.col-10
         [:img {:src "img/schriftzug-dpb.svg"
                :style {:width "100%"}}]]]
       [:h4.text-center {:style {:padding-top 0}} "Anmeldung zum"]
       [:h1.text-center "Knappen-Späher-Ritter Lager"]
       [:h4.text-center {:style {:padding-top 0}} "im sonnigen Remscheid"]
       [:br]
       body]]])))

(def footer
  [:div
   [:hr {:style {:margin-top "3rem"}}]
   [:h2 "Wichtige Informationen"]
   [:br]
   [:a.btn.btn-light {:href "pdf/einladung-ksr.pdf"}
    "Einladung als PDF"]

   [:h4 "Anreise"]
   [:p "Adresse: Hammertal 4, 42857 Remscheid"]
   [:p "Der Bahnhof \"Remscheid-Güldenwerth\" ist ca. 10 Minuten fußläufig über einen Wanderweg erreichbar."]
   [:p "Bei Anreise mit dem "
    [:strong "Auto"]
    " parkt ihr bitte dem P&R Parkplatz am Bahnhof Güldenwerth und wandert die paar Meter zum Kotten."]
   [:div.myIframe
    [:iframe {:src "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d2501.8611792782926!2d7.165978715930733!3d51.166348643402436!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x47b92a86dce4928d%3A0x94cced3261c0deb0!2sDiedrichs-Kotten!5e0!3m2!1sde!2sde!4v1511819351039"
              :frameborder 0
              :allowfullscreen true
              :style {:border 0}}]]

   [:h4 "Shuttle"]
   [:p "Solltet ihr Shuttlebedarf haben, so meldet euch unter "
    [:a {:href "mailto:shuttle@dpb-remscheid.de"} "shuttle@dpb-remscheid.de"]
    "."]])

(defn home-page [request]
  (with-header
   [:div.text-center "Es sind nur noch " [:strong (+ 10 (rand-int 20))] " Plätze verfügbar!"]
   [:br]
   [:form#form {:action "/registrieren" :method :POST}
    [:div.form-group
     [:label "Name *"]
     [:input.form-control {:name "name" :required true}]]
    [:div.form-group
     [:label "Einheit *"]
     [:input.form-control {:name "einheit" :required true}]]
    [:div.form-group
     [:label "Stand *"]
     [:select.form-control {:name "stand" :required true}
      [:option {:value ""} ""]
      [:option {:value "Jungwolf"} "Jungwolf"]
      [:option {:value "Knappe"} "Knappe"]
      [:option {:value "Späher"} "Späher"]
      [:option {:value "St.-Georgs-Knappe"} "St.-Georgs-Knappe"]
      [:option {:value "Ordensritter"} "Ordensritter"]
      [:option {:value "St.-Georgs-Ritter"} "St.-Georgs-Ritter"]
      [:option {:value "Sonstiges"} "Sonstiges"]]]
    [:div.form-group
     [:label "Essensbesonderheiten"]
     [:input.form-control {:name "essen-besonderheiten"}]]
    [:div.form-group
     [:label "Das letzte Thema in meinem Ständekreis war..."]
     [:textarea.form-control {:name "das-letzte-thema-staendekreis" :rows 3}]]
    [:div.form-group
     [:label "Orden ist für mich..."]
     [:textarea.form-control {:name "orden-ist-fuer-mich" :rows 3}]]
    [:button {:class "btn btn-primary g-recaptcha"
              :data-sitekey "6LdAujoUAAAAAHFeE4cFmwC6FriiZgVDeQx32T9M"
              :data-callback "onSubmit"} "Abschicken"]
    [:input {:class "btn btn-primary"
             :type :submit
             :value "Anmelden"}]]

   footer))

(defn register-page [{{:keys [name einheit stand
                              essen-besonderheiten
                              das-letzte-thema-staendekreis]} :form-params}]
  (with-header
    [:div.alert.alert-info
     "Nun steht dein Name auf unserer Liste, " name ". Wir freuen uns schon auf dich!"]
    footer))


;; -----------------------------------------------------------------------------

(def common-interceptors [(body-params/body-params) http/html-body])

(def routes #{["/" :get (conj common-interceptors `home-page)]
              ["/registrieren" :post (conj common-interceptors `register-page)]})

(def service {:env :prod
              ::http/routes routes
              ::http/resource-path "/public"
              ::http/type :jetty
              ::http/port 8080
              ::http/container-options {:h2c? true
                                        :h2? false
                                        :ssl? false}})

