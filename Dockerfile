FROM java:8-alpine

ADD target/ksr.jar /ksr/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/ksr/app.jar"]
