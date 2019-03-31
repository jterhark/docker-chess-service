FROM java:8
WORKDIR /
ADD out/artifacts/main_jar/main.jar main.jar
EXPOSE 8080
CMD java - jar main.jar
