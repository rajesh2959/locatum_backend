package com.semaifour.facesix.beacon.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.util.CustomerUtils;
import net.sf.json.JSONObject;

@Service
public class TagTypeCacheService {

static Logger logger = LoggerFactory.getLogger(TagTypeCacheService.class.getName());
	
	@Autowired
	private TagTypeService tagTypeService;
	
	@Autowired
	private CustomerService customerService;
	
	private static List<String> locatumSolutionList = CustomerUtils.locatumSolutionList;

	/**
	 * Used to load the tagType into cache
	 */
	
	private final static String defaultCid  = "default";
	private static String tagType 			= "Employee";
	private static String TagIconColor 		= "lime";
	private static String tagIcon  			= "\uf007";
	
	static List<TagType> defaultTagTypes = new ArrayList<TagType>();
	
	@PostConstruct
	private void init() {
		
		// Load tagType into cache
		
		Iterable<Customer>  customerlist = customerService.findBySolutionAndStatus(locatumSolutionList,"ACTIVE");
		loadTagTypes(customerlist);
		 
		// load default tagType into database
		
		
		List<TagType> tagTypeList = tagTypeService.findByCid(defaultCid);
		
		if (CollectionUtils.isEmpty(tagTypeList)) {
			
			TagType tagType = new TagType();
			tagType.setCid(defaultCid);
			tagType.setTagIcon("\uf007");
			tagType.setTagIconColor("cyan");
			tagType.setTagType("Contractor");
			tagType.setCreatedBy("cloud");
			tagType.setCreatedOn(new Date());
			tagType.setModifiedBy("cloud");
			tagType.setModifiedOn(new Date());
			defaultTagTypes.add(tagType);
			
			tagType = new TagType();
			tagType.setCid(defaultCid);
			tagType.setTagIcon("\uf007");
			tagType.setTagIconColor("lime");
			tagType.setTagType("Employee");
			tagType.setCreatedBy("cloud");
			tagType.setCreatedOn(new Date());
			tagType.setModifiedBy("cloud");
			tagType.setModifiedOn(new Date());
			defaultTagTypes.add(tagType);
			
			tagType = new TagType();
			tagType.setCid(defaultCid);
			tagType.setTagIcon("\uf007");
			tagType.setTagIconColor("forestgreen");
			tagType.setTagType("Visitor");
			tagType.setCreatedBy("cloud");
			tagType.setCreatedOn(new Date());
			tagType.setModifiedBy("cloud");
			tagType.setModifiedOn(new Date());
			defaultTagTypes.add(tagType);
			
			tagTypeService.save(defaultTagTypes);
		}
	}
	
	public static ConcurrentHashMap<String, ConcurrentHashMap<String,JSONObject>> tagTypeCache = new ConcurrentHashMap<String,ConcurrentHashMap<String,JSONObject>>();
	
	/**
	 * Default tagType
	 */
	
	public JSONObject getCacheTagTypeAttributes(String cid, String tagType) {
		if (StringUtils.isNotEmpty(cid) && tagTypeCache.containsKey(cid)) {
			if (tagTypeCache.get(cid).containsKey(tagType)) {
				return tagTypeCache.get(cid).get(tagType);
			}
		}
		return getDefaultCacheTagTypeAttributes();
	}

	public JSONObject getDefaultCacheTagTypeAttributes() {
		
		JSONObject payload = new JSONObject();
		
		List<TagType> tagTypeList = tagTypeService.findByCid(defaultCid);
		
		if (CollectionUtils.isNotEmpty(tagTypeList)) {
			TagType tag 	= tagTypeList.get(0);
			tagIcon 		= tag.getTagIcon();
			TagIconColor 	= tag.getTagIconColor();
			tagType		 	= tag.getTagType();
		}
		
		payload.put("tagIcon", 		tagIcon);
		payload.put("tagIconColor", TagIconColor);
		payload.put("tagType", 		tagType);
		
		return payload;
	}
	
	public List<TagType> getDefaultgCustomerTagTypeAttributes() {
		List<TagType> tagTypeList = tagTypeService.findByCid(defaultCid);
		if (CollectionUtils.isNotEmpty(tagTypeList)) {
			return tagTypeList;
		} else {
			return defaultTagTypes;
		}
	}

	public void  updateCacheTagAttributes(String cid, List<TagType> tagTypeList) {
		if (CollectionUtils.isNotEmpty(tagTypeList) && StringUtils.isNotEmpty(cid)) {
			ConcurrentHashMap<String, JSONObject> tagMap = new ConcurrentHashMap<String, JSONObject>();
			for (TagType tagType : tagTypeList) {
				JSONObject payload = new JSONObject();
				payload.put("tagIcon", tagType.getTagIcon());
				payload.put("tagIconColor", tagType.getTagIconColor());
				tagMap.put(tagType.getTagType(), payload);
				//logger.info("TagType " + tagType.getTagType());
			}
			tagTypeCache.put(cid, tagMap);
		} else {
			logger.info("cid " + cid + " tagTypeList " + tagTypeList);
		}
	}
	
	public boolean loadTagTypes(Iterable<Customer>  customerlist) {
		try {

			if (customerlist != null) {
				for (Customer customer : customerlist) {
					String cxId = customer.getId();
					List<TagType> tagTypeList = tagTypeService.findByCid(cxId);
					if (CollectionUtils.isNotEmpty(tagTypeList)) {
						updateCacheTagAttributes(cxId, tagTypeList);
					} else {
						//logger.info("TagType empty for this customer " + customer.getCustomerName());
					}
				}
				return true;
			} else {
				logger.info("customerlist " + customerlist);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
		return false;
	}
	
	public ConcurrentHashMap<String,JSONObject> deleteTagTypeFromCache(String cid) {
		if (StringUtils.isNotBlank(cid) && TagTypeCacheService.tagTypeCache.containsKey(cid)) {
			TagTypeCacheService.tagTypeCache.get(cid).clear();
		} else {
			logger.info("TagType is empty");
		}
		
		return TagTypeCacheService.tagTypeCache.get(cid);
	}
}
