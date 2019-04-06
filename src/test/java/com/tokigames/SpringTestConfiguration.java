package com.tokigames;

import com.mongodb.MongoClient;
import com.tokigames.config.ApplicationConfig;
import com.tokigames.service.HttpRequestUtil;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.web.client.RestTemplate;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.tokigames.service")
@Slf4j
public class SpringTestConfiguration implements DisposableBean {

  public static final String CHEAP_FLIGHTS_URL = "cheap_flights_url";
  public static final String BUSINESS_FLIGHTS_URL = "business_flights_url";

  private static final MongodStarter mongodStarter = MongodStarter.getDefaultInstance();
  private MongodExecutable mongodExecutable;
  private MongodProcess mongodProcess;

  public void destroy() {
    if (mongodExecutable != null) {
      mongodExecutable.stop();
    }
    if (mongodProcess != null) {
      mongodProcess.stop();
    }
  }

  @Bean
  public ApplicationConfig applicationConfig() {
    return new ApplicationConfig(CHEAP_FLIGHTS_URL, BUSINESS_FLIGHTS_URL);
  }

  @MockBean
  public RestTemplate restTemplate;

  @Bean
  public MongoClient mongoClient() {
    return new MongoClient("localhost", 12345);
  }

  @Bean
  public MongoDbFactory mongoDbFactory() throws IOException {
    log.info("trying to start local mock mongodb...");
    mongodExecutable = mongodStarter.prepare(new MongodConfigBuilder().version(Version.V3_6_5).net(new Net("localhost", 12345, Network.localhostIsIPv6())).build());
    mongodProcess = mongodExecutable.start();

    return new SimpleMongoDbFactory(mongoClient(), "content");
  }

  @Bean
  public MongoTemplate mongoTemplate(MongoDbFactory mongoDbFactory, MappingMongoConverter mappingMongoConverter) {
    return new MongoTemplate(mongoDbFactory, mappingMongoConverter);
  }

  @Bean
  public HttpRequestUtil httpRequestUtil() {
    return Mockito.mock(HttpRequestUtil.class);
  }

}
