FROM clojure:openjdk-14-tools-deps-alpine

WORKDIR /code

COPY deps.edn .
RUN clojure -Sdeps '{:mvn/local-repo "./.m2/repository"}' -e "(prn \"Downloading deps\")"

RUN apk add yarn make
RUN yarn global add sass

COPY package.json .
COPY yarn.lock .
COPY node_modules/ .
RUN yarn install

COPY . .

RUN sass ./resources/public/css/main.scss ./resources/public/css/main.css --no-source-map --style compressed

RUN make jar

EXPOSE 8080

CMD ["java", "-jar", "target/registration-page.jar"]
