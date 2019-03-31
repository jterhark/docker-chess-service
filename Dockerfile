FROM java:8
WORKDIR /
ADD out/artifacts/main_jar/main.jar main.jar
EXPOSE 8080
CMD export _JAVA_OPTIONS="-Xms1024m -Xmx1024m" && java -jar main.jar
