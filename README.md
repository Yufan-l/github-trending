# Github Trending Web Application

A simple Vert.x application to show top github repos by their fork numbers. 


## Build

### Build without docker

You can build the project using maven:

```
mvn clean install
```
### Build with docker

You can build the project using docker:

```
docker build . -t github-trending
```
## Run
### run without docker
Once built, just launch the _fat jar_ as follows:

```
java -jar /target/vertx.github.trending-0.0.1-SNAPSHOT-fat.jar
```

### run with docker
```
docker run -it -p 8080:8080 github-trending
```


Then, open a browser to http://localhost:8080/assets.

