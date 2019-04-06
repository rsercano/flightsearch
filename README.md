## Flight Search
This is a demonstration of assessment by TokiGames, uses below URLS to fetch flights;

- https://obscure-caverns-79008.herokuapp.com/cheap
- https://obscure-caverns-79008.herokuapp.com/business

### Build & Execution
This project internally uses [maven wrapper](https://github.com/takari/maven-wrapper), therefore you can build & run it with;

``./mvnw clean package``

to execute;

``java -jar target/flightsearch-1.0.0-SNAPSHOT.jar``

or with options if you don't want to use default ones;

``java -jar target/flightsearch-1.0.0-SNAPSHOT.jar -Dspring.config.location=/tmp/application.properties``

then you'll be able to access service documentation on;

`http://localhost:<server.port>/<server.servlet.context-path>/swagger-ui.html` 

in default configuration [just click here](http://localhost:8080/flightsearch/swagger-ui.html)

### Dependencies

- It requires `MongoDB 3.6+` to be up & running.
- It requires `JDK11` for execution

### Default Configuration

If you don't provide an application.properties file the following configuration will be used;

```
spring.data.mongodb.uri=mongodb://localhost:27017/flightsearch
server.port=8080
server.servlet.context-path=/flightsearch
logging.config=logback.xml
cheapflights.url=https://obscure-caverns-79008.herokuapp.com/cheap
businessflights.url=https://obscure-caverns-79008.herokuapp.com/business
```

#### Logging file

The default logging file is a logback configuration file which uses the following configuration;

```
<?xml version="1.0" encoding="UTF-8"?>

<configuration>
	<logger name="springfox.documentation" level="WARN"/>
	<logger name="org.mongodb" level="DEBUG"/>
	<logger name="org.springframework" level="INFO" />
	<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
		<Target>System.out</Target>
		<encoder>
			<pattern>%d{ABSOLUTE} %5p %c - [%X{IP_ADDRESS}] [%X{SESSION_ID}]
				[%X{LOGGED_IN_USER}] %m%n</pattern>
		</encoder>
		<filter class="ch.qos.logback.classic.filter.ThresholdFilter">
			<level>INFO</level>
		</filter>
	</appender>
	
	<root level="DEBUG">
		<appender-ref ref="CONSOLE" />
	</root>
</configuration>
```

*Beware that there's no file appender in the default logging configuration.*

### Usage

#### Cache
It internally caches two given external sources on startup,
if you want to re-cache you can just send a `GET` request to `http://localhost:8080/flightsearch/flight/cache`



### Code Guidelines
This project follows the guidelines [from here](https://sercan.atlassian.net/wiki/spaces/JAVA/pages/687603713/a.+Code+Quality) 

Uses;

- [Lombok](https://sercan.atlassian.net/wiki/spaces/JAVA/pages/687570950/2.+Lombok+Plugin)
- [Checkstyle (Google style)](https://sercan.atlassian.net/wiki/spaces/JAVA/pages/688357379/3.+Checkstyle)
- [Google code formatter](https://sercan.atlassian.net/wiki/spaces/JAVA/pages/687374338/1.+Conventions+Indention+Code+Style+-+Google+Style) 


