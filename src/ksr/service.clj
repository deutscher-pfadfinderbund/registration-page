(ns ksr.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]
            [hiccup.page :as hp]
            [clojure.spec.alpha :as s]
            [geheimtur.interceptor :as gti]))

(def state (atom {:teilnehmer []}))

(defn store-participant! [user]
  (spit "database.edn" (swap! state update-in [:teilnehmer] conj user)))

(s/fdef store-participant!
        :args (s/cat :user ::user))

(defn load-database! []
  (reset! state (read-string (slurp "database.edn"))))


;; -----------------------------------------------------------------------------

(defn with-header [& body]
  (ring-resp/response
   (hp/html5
    (hp/include-css "css/bootstrap.min.css")
    (hp/include-css "css/main.css")
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
                   .bindTooltip('<b>Diederichskotten</b><br>Besser ihr wachst eure Juja noch einmal frisch')
                   .openTooltip();

     L.polygon([
       [51.16957, 7.16347],
       [51.16940, 7.16312],
       [51.16903, 7.16326],
       [51.16910, 7.16381]
     ]).addTo(mymap).bindTooltip('P&R Parkplätze');

     var popup = L.popup();

     var baseMaps = {
       'Mapbox':                         L.mapbox.tileLayer('mapbox.streets'),
       'OSM':                            L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'),
       'Satellit mit Straßen':           L.mapbox.tileLayer('mapbox.streets-satellite'),
       'Zum Wandern und Fahrrad fahren': L.mapbox.tileLayer('mapbox.run-bike-hike')
     };

     L.control.layers(baseMaps).addTo(mymap);
     L.control.fullscreen().addTo(mymap);
     L.control.scale().addTo(mymap);
"]])

(def footer
  [:div
   [:hr {:style {:margin-top "3rem"}}]
   [:h2 "Wichtige Informationen"]
   [:h4 "Einladung"]
   [:div.card.card-1
    [:a {:href "pdf/einladung-ksr.pdf"}
     [:img.hover.img-fluid
      {:src "pdf/einladung-ksr.png"
       :style {:width "300px"}}]]]

   [:hr]
   [:h4 "Wetterbericht"]
   [:video {:width 640 :controls true}
    [:source {:src "vid/remscheiderwetter.mp4" :type "video/mp4"}]]

   [:hr]
   [:h4 "Anreise"]
   [:p "Adresse: Hammertal 4, 42857 Remscheid"]
   [:p "Der Bahnhof \"Remscheid-Güldenwerth\" ist ca. 10 Minuten fußläufig über einen Wanderweg erreichbar."]
   [:p "Bei Anreise mit dem "
    [:strong "Auto"]
    " parkt ihr bitte dem P&R Parkplatz am Bahnhof Güldenwerth und wandert die paar Meter zum Kotten."]
   [:div#map]

   [:hr]
   [:h4 "Shuttle"]
   [:p "Solltet ihr Shuttlebedarf haben, so meldet euch unter "
    [:a {:href "mailto:shuttle@dpb-remscheid.de"} "shuttle@dpb-remscheid.de"]
    "."]
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
  (store-participant! (:form-params request))
  (with-header
    [:div.alert.alert-info {:style {:margin "3rem 0"}}
     "Nun steht dein Name auf unserer Liste, " name ". Wir freuen uns schon auf dich!"]
    [:a.btn.btn-light {:href "https://ksr.deutscher-pfadfinderbund.de"}
     "Zurück und tolle Informationen nachlesen"]))


;; -----------------------------------------------------------------------------
;; Admin

(defn users-to-rows []
  (for [user (:teilnehmer @state)]
    [:tr [:td (:name user)][:td (:einheit user)][:td (:stand user)]]))

(defn admin-page [request]
  (with-header
    [:h2 "Anmeldungen"]
    [:br][:br]
    [:table.table.table-striped
     [:thead [:tr [:th "Name"][:th "Einheit"][:th "Stand"]]]
     [:tbody (users-to-rows)]]))


;; -----------------------------------------------------------------------------

(def users {"ksr" {:password "diskette"
                     :roles #{:ksr}
                     :full-name "Knäppchen"}})

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
              ::http/container-options {:h2c? true
                                        :h2? false
                                        :ssl? false}})

;; -----------------------------------------------------------------------------

(s/def ::name string?)
(s/def ::einheit string?)
(s/def ::essen-besonderheiten string?)
(s/def ::das-letzte-thema-staendekreis string?)
(s/def ::orden-ist-fuer-mich string?)
(s/def ::user
  (s/keys :req-un [::name ::einheit ::essen-besonderheiten
                   ::das-letzte-thema-staendekreis ::orden-ist-fuer-mich]))
