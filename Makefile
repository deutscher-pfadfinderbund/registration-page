tests:
	clj -A:dev:test

run:
	clj -M -m ksr.core

jar:
	clojure -A:uberjar

image:
	docker build -t ghcr.io/deutscher-pfadfinderbund/registration-page .