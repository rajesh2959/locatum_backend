package com.semaifour.facesix.mesh.service;

import java.text.MessageFormat;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.semaifour.facesix.mqtt.DeviceEventPublisher;

@Service
public class MeshMonitorMQTTService {

	static Logger logger = LoggerFactory.getLogger(MeshMonitorMQTTService.class.getName());
	
	@Autowired private DeviceEventPublisher mqttPublisher;
	
	private static final String SIMULATION_OFF  = "simulation_off";
	private static final String SIMULATION_ON   = "simulation";
	
	public final ConcurrentHashMap<String, JSONObject> simulationPersistenceCache = new ConcurrentHashMap<String,JSONObject>();
	
	public void simulationOff(JSONObject param) {
		
		String uid 			= (String)param.get("uid");
		String mqttTemplate = " \"opcode\":\"{0}\"";
		
		String message = MessageFormat.format(mqttTemplate, new Object[] { SIMULATION_OFF });
		mqttPublisher.publish("{" + message + "}", uid);
		
		if (simulationPersistenceCache.containsKey(uid)) {
			simulationPersistenceCache.remove(uid);
			logger.info("simulation payload removed " + uid);
		} else {
			logger.info("Given uid simulation payload not found " + uid);
		}

	}

	public void basic(JSONObject param) {
		
		String uid 					= (String)param.get("uid");
		String free_cpu 			= (String)param.get("free_cpu");
		String free_ram 			= (String)param.get("free_ram");
		String batt_remaining_time  = (String)param.get("batt_remaining_time");
		String primary_link_rssi    = (String)param.get("primary_link_rssi");
		
		String mqttMsgTemplate = " \"opcode\":\"{0}\", \"free_cpu\":\"{1}\", \"free_ram\":\"{2}\","
				+ " \"batt_remaining_time\":\"{3}\", \"primary_link_rssi\":\"{4}\", \"hops_count\":\"{5}\" ";
		
		
		String hops_count = (String)param.get("hops_count");

		if (StringUtils.isEmpty(free_cpu))
			free_cpu = "-1";
		if (StringUtils.isEmpty(free_ram))
			free_ram = "-1";
		if (StringUtils.isEmpty(primary_link_rssi))
			primary_link_rssi = "255";
		if (StringUtils.isEmpty(hops_count))
			hops_count = "-1";
		if (StringUtils.isEmpty(batt_remaining_time))
			batt_remaining_time = "-1";
		
		String message = MessageFormat.format(mqttMsgTemplate, new Object[] { SIMULATION_ON,free_cpu,free_ram,batt_remaining_time,primary_link_rssi,hops_count });
		mqttPublisher.publish("{" + message + "}", uid);
		
		
		simulationPersistenceCache.put(uid, param);
		  
	}

	public void gaming(JSONObject param) {

		String uid 	    = (String)param.get("uid");
		String free_cpu = (String) param.get("free_cpu");
		String free_ram = (String) param.get("free_ram");
		String batt_remaining_time = (String) param.get("batt_remaining_time");

		if (StringUtils.isEmpty(free_cpu))
			free_cpu = "15";
		if (StringUtils.isEmpty(free_ram))
			free_ram = "20";
		if (StringUtils.isEmpty(batt_remaining_time))
			batt_remaining_time = "120";

		String mqttMsgTemplate = " \"opcode\":\"{0}\", \"free_cpu\":\"{1}\", \"free_ram\":\"{2}\", \"batt_remaining_time\":\"{3}\"";

		String message = MessageFormat.format(mqttMsgTemplate,new Object[] { SIMULATION_ON, free_cpu, free_ram, batt_remaining_time });
		mqttPublisher.publish("{" + message + "}",uid);
		
		simulationPersistenceCache.put(uid, param);

	}

	public void moderate(JSONObject param) {

		String uid 		= (String)param.get("uid");
		String free_cpu = (String) param.get("free_cpu");
		String free_ram = (String) param.get("free_ram");
		String batt_remaining_time = (String) param.get("batt_remaining_time");

		if (StringUtils.isEmpty(free_cpu))
			free_cpu = "50";
		if (StringUtils.isEmpty(free_ram))
			free_ram = "50";
		if (StringUtils.isEmpty(batt_remaining_time))
			batt_remaining_time = "240";

		String mqttMsgTemplate = " \"opcode\":\"{0}\", \"free_cpu\":\"{1}\", \"free_ram\":\"{2}\", \"batt_remaining_time\":\"{3}\"";

		String message = MessageFormat.format(mqttMsgTemplate,	new Object[] { SIMULATION_ON, free_cpu, free_ram, batt_remaining_time });
		mqttPublisher.publish("{" + message + "}",uid);
		
		simulationPersistenceCache.put(uid, param);

	}

	public void idle(JSONObject param) {

		String uid 		= (String)param.get("uid");
		String free_cpu = (String) param.get("free_cpu");
		String free_ram = (String) param.get("free_ram");

		if (StringUtils.isEmpty(free_cpu))
			free_cpu = "90";
		if (StringUtils.isEmpty(free_ram))
			free_ram = "90";

		String mqttMsgTemplate = " \"opcode\":\"{0}\", \"free_cpu\":\"{1}\", \"free_ram\":\"{2}\"";

		String message = MessageFormat.format(mqttMsgTemplate, new Object[] { SIMULATION_ON, free_cpu, free_ram });
		mqttPublisher.publish("{" + message + "}", uid);
		
		simulationPersistenceCache.put(uid, param);

	}
	
}
