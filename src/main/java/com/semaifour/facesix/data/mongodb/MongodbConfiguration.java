package com.semaifour.facesix.data.mongodb;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Component;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.semaifour.facesix.spring.ApplicationProperties;


/**
 * MongodbConfiguration Configuration
 * 
 * @author mjs
 *
 */

@Component
@EnableMongoRepositories(basePackages = "com.semaifour.facesix")
public class MongodbConfiguration {
	
	static Logger LOG = LoggerFactory.getLogger(MongodbConfiguration.class.getName());
	
	private List<ServerAddress> hosts = new ArrayList<ServerAddress>();;
	
	@Autowired
	ApplicationProperties properties;
	
	public ApplicationProperties properties() {
		return properties;
	}
	
	@PostConstruct
	public void init() {
		String tmp = properties.getProperty("mongo.hosts");
		if (tmp != null) {
			LOG.info("Mongo is enabled :"  + tmp);
			String[] hs = tmp.split(",");
			for (String s : hs) {
				String[] ss = s.split(":");
				try {
					hosts.add(new ServerAddress(ss[0], Integer.parseInt(ss[1])));
				} catch (Exception e) {
					LOG.warn("Failed to connect to mongo :" + s,  e );
				}
			}
		} else {
			LOG.info("Mongo is disabled");
		}
	}
	
	public @Bean MongoDbFactory mongoDbFactory() throws Exception {
		if (hosts.size() > 0) {
			LOG.info("connecting to mongo");
			return new SimpleMongoDbFactory(new MongoClient(hosts), properties.getProperty("mongo.db"));
		} else {
			return null;
		}
	}

	public @Bean MongoTemplate mongoTemplate() throws Exception {
		MongoDbFactory factory = mongoDbFactory();
		if (factory != null) {
			MongoTemplate mongoTemplate = new MongoTemplate(factory);
			return mongoTemplate;
		} else {
			return null;
		}
	}
	
}