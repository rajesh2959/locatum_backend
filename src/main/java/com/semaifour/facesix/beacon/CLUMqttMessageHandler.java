package com.semaifour.facesix.beacon;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ForkJoinPool;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.beacon.rest.ProcessBeacons;
import com.semaifour.facesix.boot.Application;
import com.semaifour.facesix.data.account.UserAccount;
import com.semaifour.facesix.data.account.UserAccountService;
import com.semaifour.facesix.data.elasticsearch.ElasticService;
import com.semaifour.facesix.data.site.Portion;
import com.semaifour.facesix.data.site.PortionService;
import com.semaifour.facesix.data.site.Site;
import com.semaifour.facesix.data.site.SiteService;
import com.semaifour.facesix.mqtt.DefaultMqttMessageReceiver;
import com.semaifour.facesix.spring.CCC;
import com.semaifour.facesix.util.CustomerUtils;

public class CLUMqttMessageHandler extends DefaultMqttMessageReceiver {
	
	private static String classname = CLUMqttMessageHandler.class.getName();
	Logger LOG = LoggerFactory.getLogger(classname);
	

	ForkJoinPool forkJoinPool   = new ForkJoinPool();
	
	Map<Integer,ProcessBeacons> myRecursiveTask = new HashMap<Integer,ProcessBeacons>();
	
	@Autowired
	private static CustomerService customerService;
	
	@Autowired
	private static PortionService portionService;
	
	@Autowired
	private static SiteService siteService;
	
	@Autowired
	private static CustomerUtils customerUtils;
	
	@Autowired
	private static CCC _CCC;
	
	@Autowired
	private static ElasticService elasticService;
	
	@Autowired
	private static UserAccountService userAccountService;
	
	@Override
	public String getName() {
		return "CLUMqttMessageHandler";
	}
	
	@Override
	public boolean messageArrived(String topic, MqttMessage message) {
		return messageArrived(topic, message.toString());
	}
	
	@Override
	public boolean messageArrived(String topic, String message) {
		
		try {
			long stimer = System.currentTimeMillis();
			ObjectMapper mapper     = new ObjectMapper();
			Map<String, Object> map = mapper.readValue(message, new TypeReference<HashMap<String, Object>>(){});
			
			String op = String.valueOf(map.get("opcode"));
			
			/*
			 * date = timestamp when the data was sent
			 */
			String sentTime = map.get("server_send_ts").toString();
			
			switch (op) {
			
			case "current-location-update":
				
				boolean debugLog = false;
				
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				DateFormat parse  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
				
				if (!map.containsKey("tag_list")) {
					return false;
				}
				
				String spid 		= (String) map.get("spid");
				String serverid		= (String) map.get("uid");

				List<Map<String, Object>> tag_list = (List<Map<String, Object>>) map.get("tag_list");

				Portion portion = getPortionService().findById(spid);
				if (portion != null) {
					
					String cid = portion.getCid();
					String sid = portion.getSiteId();
					String floorname = portion.getUid();
					int height 	= portion.getHeight();
					int width 	= portion.getWidth();
					
					String venuename = "NA";
					
					Site site = getSiteService().findById(sid);
					if (site != null) {
						venuename = site.getUid();
					}
					
					Customer customer = getCustomerService().findById(cid);
					
					if (customer != null) {
						
						String ismailalert = "true";
						List<UserAccount> useracclist = getUserAccountService().findByCustomerIdAndIsMailAlert(cid, ismailalert);

						debugLog = customer.getLogs() != null && customer.getLogs().equals("true") ? true : false;
						
						getCustomerUtils().logs(debugLog, classname,"MESSAGE Tag Count ===> " + map.get("tag_count")+ "date = " +sentTime);
						
						TimeZone timeZone = getCustomerUtils().FetchTimeZone(customer.getTimezone());
						format.setTimeZone(timeZone);
						String date = format.format(new Date());
						Date parsedSentTime = parse.parse(sentTime);
						String recSentTime = format.format(parsedSentTime);
						
						ProcessBeacons processBeacons = new ProcessBeacons();
						
						processBeacons.setTagDetail(tag_list);
						processBeacons.setCustomerDetails(cid,serverid,debugLog,recSentTime);
						processBeacons.setPortionDetails(sid,venuename,spid,floorname,height,width);
						processBeacons.setTimeZone(timeZone);
						processBeacons.setUserAccountList(useracclist);
						/*
						 * record date is the field included for testing purpose to find out when was the data
						 * actually sent and when it is getting processed.
						 */
						processBeacons.setRecordSentDate(recSentTime);
						processBeacons.setRecordSeenDate(date);
						//forkJoinPool.execute(processBeacons);
					} else {
						getCustomerUtils().logs(debugLog, classname, " Customer not found given id " +cid);
					}
					
				} else {
					getCustomerUtils().logs(debugLog, classname, " Floor not found given id " +spid);
				}
				
				/*
				// removing elastic search post.
				int tagCount 		= (int)map.get("tag_count");
				int max_record		= (int)map.get("max_record");
				int record_num		= (int)map.get("record_num");
				
				HashMap<String, Object> cluPost = new HashMap<String, Object>();

				parse.setTimeZone(TimeZone.getTimeZone("UTC"));
				String formatedSentTime = parse.format(parsedSentTime);
				cluPost.put("opcode", cluReportOpcode);
				cluPost.put("uid", serverid);
				cluPost.put("spid", spid);
				cluPost.put("tag_count", tagCount);
				cluPost.put("record_num", record_num);
				cluPost.put("max_record", max_record);
				cluPost.put("tag_list", tag_list);
				cluPost.put("server_send_ts", parse.parse(formatedSentTime));
				cluPost.put("dateTime", recSentTime);
				
				if(getElasticService() != null) {
					//getCustomerUtils().logs(debugLog, classname,"CLU POST = "+cluPost);
					getElasticService().post(indexname, "cluReport", cluPost);
				}*/
				
				long eTimer = System.currentTimeMillis() - stimer;
				
				getCustomerUtils().logs(debugLog, classname, " Time taken to complete the record " + " = " + eTimer+" date = "+sentTime);

				break;
			default:
				break;
			}
		} catch(Exception e) {
			//e.printStackTrace();
			LOG.info("Service classes not yet initialised "+e.getMessage());
		}
		
		return false;
	}
	
	public CustomerService getCustomerService() {

		if (customerService == null) {
			customerService = Application.context.getBean(CustomerService.class);
		}

		return customerService;

	}
	
	public CustomerUtils getCustomerUtils() {

		if (customerUtils == null) {
			customerUtils = Application.context.getBean(CustomerUtils.class);
		}

		return customerUtils;
	}
	
	private PortionService getPortionService() {
		if (portionService == null) {
			portionService = Application.context.getBean(PortionService.class);
		}

		return portionService;
	}

	private SiteService getSiteService() {
		if (siteService == null) {
			siteService = Application.context.getBean(SiteService.class);
		}

		return siteService;
	}
	
	public CCC getCCC() {
		if (_CCC == null) {
			_CCC = Application.context.getBean(CCC.class);
		}
		return _CCC;
	}
	
	public ElasticService getElasticService() {
		if (elasticService == null) {
			elasticService = Application.context.getBean(ElasticService.class);
		}
		return elasticService;
	}
	
	public UserAccountService getUserAccountService() {

		if (userAccountService == null) {
			userAccountService = Application.context.getBean(UserAccountService.class);
		}
		return userAccountService;
	}
}
