FROM clojure:openjdk-14-tools-deps-alpine AS build

WORKDIR /code

COPY deps.edn .
RUN clojure -Sdeps '{:mvn/local-repo "./.m2/repository"}' -e "(prn \"Downloading deps\")"

RUN apk add yarn make
RUN yarn global add sass

COPY package.json .
COPY yarn.lock .
COPY resources/public/node_modules/ resources/public/node_modules
RUN yarn install

COPY . .

RUN sass ./resources/public/css/main.scss ./resources/public/css/main.css --no-source-map --style compressed

EXPOSE 8080

RUN clojure -Sdeps '{:mvn/local-repo "./.m2/repository"}' -A:uberjar

CMD ["java", "-cp", "target/registration-page.jar", "clojure.main", "-m", "ksr.core"]