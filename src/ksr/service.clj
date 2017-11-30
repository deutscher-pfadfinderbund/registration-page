(ns ksr.service
  (:require [io.pedestal.http :as http]
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
     [:title "Anmeldung Knappen-Späher-Ritter-Lager 2017 in Remscheid"]
     [:meta {:charset "utf-8"}]
     [:meta {:http-equiv "X-UA-Compatible"
             :content "IE=edge"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     (hp/include-css "css/bootstrap.min.css")
     (hp/include-css "css/main.css")]
    [:div.container
     [:div.row
      [:div.col-1.col-md-1]
      [:div.col-10.col-md-10.col-sm-12.card.card-3
       [:img {:src "img/schriftzug-dpb.svg"
              :style {:width "100%"}}]
       [:h4.text-center {:style {:padding-top 0}} "Anmeldung zum"]
       [:h1.text-center "Knappen-Späher-Ritter Lager"]
       [:h4.text-center {:style {:padding-top 0}} "im sonnigen Remscheid"]
       [:br]
       body]]
     [:div.row {:style {:padding-bottom "1rem"
                        :margin-top "0.5rem"}}
      [:div.col-md-1]
      [:div.col-md-10.col-sm-10.text-right
       [:p [:a.text-secondary {:href "/anmeldungen"} "Anmeldungen"]]]]])))

(defn build-hiking-map []
  [:div
   [:link {:rel "stylesheet"
           :href "https://unpkg.com/leaflet@1.2.0/dist/leaflet.css"
           :integrity "sha512-M2wvCLH6DSRazYeZRIm1JnYyh22purTM+FDB5CsyxtQJYeKq83arPe5wgbNmcFXGqiSH2XR8dT/fJISVA1r/zQ=="
           :crossorigin ""}]
   [:script {:src "https://unpkg.com/leaflet@1.2.0/dist/leaflet.js"
             :integrity "sha512-lInM/apFSqyy1o6s89K4iQUKg6ppXEgsVxT35HbzUupEVRh2Eu9Wdl4tHj7dZO0s1uvplcYGmt3498TtHq+log=="
             :crossorigin ""}]
   (hp/include-js "https://api.mapbox.com/mapbox.js/v2.2.2/mapbox.js")
   (hp/include-css "https://api.mapbox.com/mapbox.js/v2.2.2/mapbox.css")
   (hp/include-js "https://api.mapbox.com/mapbox.js/plugins/leaflet-fullscreen/v0.0.4/Leaflet.fullscreen.min.js")
   (hp/include-css "https://api.mapbox.com/mapbox.js/plugins/leaflet-fullscreen/v0.0.4/leaflet.fullscreen.css")
   [:script "
     L.mapbox.accessToken = 'pk.eyJ1IjoibjJvIiwiYSI6ImNpZWd6c2NrMDAwMHBzd204NW41bmFsNHUifQ.XstEiEGH9bP2wasGOOVp4g';

     var mymap = L.map('map', {
        center: [51.16778, 7.16582],
        zoom: 16,
        layers: [L.mapbox.tileLayer('mapbox.streets')],  // Set default map
        maxZoom: 19,
        minZoom: 0
      });

     var kotten = L.marker([51.16630, 7.16825])
                   .addTo(mymap)
                   .bindPopup('<b>Diederichskotten</b><br>Besser ihr wachst eure Juja noch einmal frisch')
                   .openPopup();

     L.polygon([
       [51.16957, 7.16347],
       [51.16929, 7.16281],
       [51.16890, 7.16302],
       [51.16910, 7.16381]
     ]).addTo(mymap).bindPopup('Parkplätze Remscheid-Güldenwerth');

     var popup = L.popup();

     var baseMaps = {
       'Mapbox':                         L.mapbox.tileLayer('mapbox.streets'),
       'OSM':                            L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'),
       'Satellit mit Straßen':           L.mapbox.tileLayer('mapbox.streets-satellite'),
       'Zum Wandern und Fahrrad fahren': L.mapbox.tileLayer('mapbox.run-bike-hike')
     };

     L.control.layers(baseMaps).addTo(mymap);
     L.control.fullscreen().addTo(mymap);
     L.control.scale().addTo(mymap);"]])

(def footer
  [:div
   [:hr {:style {:margin-top "3rem"}}]
   [:h2 "Wichtige Informationen"]
   [:div.row
    [:div.col-12.col-md-6
     [:h3 "Einladung"]
     [:div.card.card-1
      [:a {:href "pdf/einladung-ksr.pdf"
           :target "_blank"}
       [:img.hover.img-fluid
        {:src "pdf/einladung-ksr.png"
         :style {:width "300px"}}]]]]
    [:div.col-12.col-md-6
     [:h3 "Eckdaten"]
     [:h4 "Wann?"][:p "27.04. - 29.04.2018"]
     [:h4 "Preis?"][:p "Wissen wir noch nicht"]
     [:h4 "Sollte ich teilnehmen?"][:p "Ja"]]]

   [:hr]
   [:h3 "Wetterbericht"]
   [:video {:width "100%" :controls true}
    [:source {:src "vid/remscheiderwetter.mp4" :type "video/mp4"}]]

   [:hr]
   [:h3 "Anreise"]
   [:div.row {:style {:padding-top "1rem"}}
    [:div.col-12.col-md-6
     [:h4 "Auto"]
     [:p "Bei Anreise mit dem Auto parkt ihr bitte dem P&R Parkplatz am Bahnhof
    \"Remscheid-Güldenwerth\" und wandert die paar Meter zum Kotten. Es gibt am
    Kotten nur sehr wenige Parkplätze."]]

    [:div.col-12.col-md-6
     [:h4 "ÖPNV"]
     [:p "Fahrt bis zum Bahnhof \"Remscheid-Güldenwerth\" (nicht zum
   Hauptbahnhof!). Von Güldenwerth aus geht es ca. 10-15 Minuten über einen
   Wanderweg zum Kotten."]]]

   [:div.row
    [:div.col
     [:div.card.card-1
      [:strong "Adresse vom Parkplatz"][:br]
      [:a {:href "https://www.google.de/maps/place/G%C3%BCldenwerth+25,+42857+Remscheid/@51.1694412,7.1612363,278m/data=!3m2!1e3!4b1!4m5!3m4!1s0x47b92a800506e429:0x917b7db747322af8!8m2!3d51.16944!4d7.162?hl=de"
           :target "_blank"}
       "Güldenwerth 25, 42857 Remscheid"]]]
    [:div.col
     [:div.card.card-1
      [:strong "Adresse vom Kotten"][:br]
      [:a {:href "https://www.google.de/maps/place/Am+Kotten,+Hammertal+4,+42857+Remscheid/@51.1662742,7.1661182,798m/data=!3m2!1e3!4b1!4m5!3m4!1s0x47b92a86c4d2b9e5:0x33d3d6b74975abf4!8m2!3d51.166365!4d7.1681462?hl=de"
           :target "_blank"}
       "Hammertal 4, 42857 Remscheid"]]]]
   [:br][:br]

   [:div#map {:style {:height "600px"}}]
   (build-hiking-map)])


;; -----------------------------------------------------------------------------

(defn home-page [request]
  (with-header
    [:div.text-center "Es sind nur noch " [:strong (+ 10 (rand-int 20))] " Plätze verfügbar!"]
    [:br]
    [:form#demo-form {:action "/registrieren" :method :POST}
     [:div.form-group
      [:label#name "Name *"]
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
     [:input {:class "btn btn-primary"
              :type :submit
              :value "Anmelden"}]]
    footer))

(defn register-page [{{:keys [name]} :form-params :as request}]
  (db/add-participant! (:form-params request))
  (with-header
    [:div.alert.alert-info {:style {:margin "3rem 0"}}
     "Nun steht dein Name auf unserer Liste, " name ". Wir freuen uns schon auf dich!"]
    [:a.btn.btn-light {:href "/"}
     "Zurück und tolle Informationen nachlesen"]))


;; -----------------------------------------------------------------------------
;; Admin

(defn users-to-rows []
  (for [{:keys [name einheit stand]} (db/get-participants)
        :when (not (nil? name))]
    [:tr [:td name][:td einheit][:td stand]]))

(defn admin-page [request]
  (with-header
    [:h2 "Anmeldungen"]
    [:br][:br]
    [:table.table.table-striped
     [:thead [:tr [:th "Name"][:th "Einheit"][:th "Stand"]]]
     [:tbody (users-to-rows)]]))


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
      (dissoc identity :password ))))

(def common-interceptors [(body-params/body-params) http/html-body])
(def http-basic-interceptors (into common-interceptors [(gti/http-basic "... Nope." credentials)]))

(def routes #{["/" :get (conj common-interceptors `home-page)]
              ["/registrieren" :post (conj common-interceptors `register-page)]
              ["/anmeldungen" :get (into http-basic-interceptors
                                         [(gti/guard :silent? false :roles #{:ksr}) `admin-page])]})

(def service {:env :prod
              ::http/routes routes
              ::http/resource-path "/public"
              ::http/type :jetty
              ::http/port 8080
              ::http/secure-headers {:content-security-policy-settings {:object-src "none"}}
              ::http/container-options {:h2c? true
                                        :h2? false
                                        :ssl? false}})

;; -----------------------------------------------------------------------------

(s/def ::maybe-string (s/or :string string? :nil nil?))
(s/def ::name ::maybe-string)
(s/def ::einheit ::maybe-string)
(s/def ::essen-besonderheiten ::maybe-string)
(s/def ::das-letzte-thema-staendekreis ::maybe-string)
(s/def ::orden-ist-fuer-mich ::maybe-string)
(s/def ::user
  (s/keys :req-un [::name ::einheit ::essen-besonderheiten
                   ::das-letzte-thema-staendekreis ::orden-ist-fuer-mich]))
