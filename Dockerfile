FROM maven
COPY . /src
WORKDIR /src
RUN mvn clean package
RUN mkdir /demo/ && tar -xzf target/*.tar.gz -C /demo/ --strip-components=2

FROM openjdk:8
COPY --from=0 /demo/ /demo/
WORKDIR /demo/
ENTRYPOINT ["java", "-cp", "demo*.jar:./*", "akkameetup.demo.Main"]