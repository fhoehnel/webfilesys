FROM maven:3.9.9-eclipse-temurin-17 AS build

# Install unzip for extracting the WAR file
RUN apt-get update && apt-get install -y unzip && rm -rf /var/lib/apt/lists/*

WORKDIR /build

COPY pom.xml ./
COPY maven-repository ./maven-repository

# Install project-local, non-public dependencies
RUN mvn -B install:install-file -Dfile=/build/maven-repository/com/drewnoakes/metadata-extractor/2.15.0/metadata-extractor-2.15.0.jar -DgroupId=com.drewnoakes -DartifactId=metadata-extractor -Dversion=2.15.0 -Dpackaging=jar && \
    mvn -B install:install-file -Dfile=/build/maven-repository/com/googlecode/compress-j2me/0.3/compress-j2me-0.3.jar -DgroupId=com.googlecode -DartifactId=compress-j2me -Dversion=0.3 -Dpackaging=jar && \
    mvn -B install:install-file -Dfile=/build/maven-repository/com/ice/tar/javatar/2.3/javatar-2.3.jar -DgroupId=com.ice.tar -DartifactId=javatar -Dversion=2.3 -Dpackaging=jar && \
    mvn -B install:install-file -Dfile=/build/maven-repository/com/keypoint/png-gif/1.0/png-gif-1.0.jar -DgroupId=com.keypoint -DartifactId=png-gif -Dversion=1.0 -Dpackaging=jar && \
    mvn -B install:install-file -Dfile=/build/maven-repository/mediachest/mediautil/1.0.0/mediautil-1.0.0.jar -DgroupId=mediachest -DartifactId=mediautil -Dversion=1.0.0 -Dpackaging=jar

COPY src ./src

RUN mvn -B -DskipTests package

# Explode (unpack) the WAR file into a directory
RUN mkdir -p /build/webfilesys-exploded && \
    cd /build/webfilesys-exploded && \
    unzip -q /build/target/webfilesys.war

FROM tomcat:9.0-jre17-temurin

RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the exploded WAR as a directory
COPY --from=build /build/webfilesys-exploded /usr/local/tomcat/webapps/webfilesys

EXPOSE 8080

CMD ["catalina.sh", "run"]

