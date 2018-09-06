FROM maven:3.5

COPY . /app
RUN cd /app && mvn install
    
CMD java -jar /app/target/vertx.github.trending-0.0.1-SNAPSHOT-fat.jar -conf /app/conf/conf.json