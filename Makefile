aot:
	mkdir -p classes
	clj -M -e "(compile 'ksr.server)"

tests:
	 clj -A:dev:test

run:
	 clj -M -m ksr.server

jar: aot
	clj -A:uberjar --main-class ksr.server