aot:
	mkdir -p classes
	clj -M -e "(compile 'ksr.core)"

tests:
	clj -A:dev:test

run:
	clj -M -m ksr.core

jar: aot
	clj -A:uberjar --main-class ksr.core