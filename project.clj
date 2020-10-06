(defproject ksr "1.1.1"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/test.check "1.1.0"]

                 [hiccup "2.0.0-alpha2"]

                 [korma "0.4.3"]
                 [org.xerial/sqlite-jdbc "3.32.3.2"]

                 [io.pedestal/pedestal.service "0.5.8"]
                 [geheimtur "0.4.0"]

                 [clj-http "3.10.3"]
                 [org.clojure/data.json "1.0.0"]

                 [io.pedestal/pedestal.jetty "0.5.8"]
                 [ch.qos.logback/logback-classic "1.2.3" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.30"]
                 [org.slf4j/jcl-over-slf4j "1.7.30"]
                 [org.slf4j/log4j-over-slf4j "1.7.30"]]
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  :jvm-opts ["-Dfile.encoding=UTF-8"]
  :profiles {:dev {:aliases {"run-dev" ["trampoline" "run" "-m" "ksr.server/run-dev"]}
                   :dependencies [[io.pedestal/pedestal.service-tools "0.5.8"]]}
             :uberjar {:aot [ksr.server]}}
  :repl-options {:init (do (require 'ksr.server)
                           (def the-server (ksr.server/run-dev)))}
  :main ^{:skip-aot true} ksr.server)

