package com.semaifour.facesix.account;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;


public interface CustomerRepository  extends MongoRepository<Customer, String>  {

	public List<Customer> findByCustomerName(String customerName);
	
	public List<Customer> findOneById(String id);
	
	public Customer findById(String id);
	
	public Customer findByEmail(String emailId);
	
	public Customer findOneByJwtrestToken(String restToken);
	
	public Customer findOneByJwtmqttToken(String restToken);

	@Query("{venueType:{$in:?0},status:?1}")
	public Iterable<Customer> findByVenueTypeAndStatus(List<String> venueType, String status);

	public Customer findByPreferedUrlName(String preferredUrl);

	@Query("{solution:{$in:?0},status:?1}")
	public Iterable<Customer> findBySolutionAndStatus(List<String> solution, String status);

	public Customer findOneByRestToken(String restToken);

	public Customer findOneByMqttToken(String mqttToken);

	/*@Query("{$or:[{restToken:?0},{mqttToken:?1}]}")
	public Customer findOneByRestTokenOrMqttToken(String restToken,String mqttToken);*/

}
