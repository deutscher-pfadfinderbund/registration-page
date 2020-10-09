tests:
	clj -A:dev:test

run:
	clj -M -m ksr.core

jar:
	clojure -A:uberjar