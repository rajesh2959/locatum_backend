package com.semaifour.facesix.beacon.rest;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.beacon.data.TagType;
import com.semaifour.facesix.beacon.data.TagTypeCacheService;
import com.semaifour.facesix.beacon.data.TagTypeService;
import com.semaifour.facesix.domain.Restponse;
import com.semaifour.facesix.util.SessionUtil;
import com.semaifour.facesix.web.WebController;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Qubercomm Inc
 *  created on 2019/04/24
 *
 */

@RestController
@RequestMapping("/rest/tagtype")
public class TagTypeRestController extends WebController {

	static Logger logger = LoggerFactory.getLogger(TagTypeRestController.class.getName());
	
	@Autowired
	private TagTypeService tagTypeService;
	
	@Autowired
	private TagTypeCacheService tagTypeCacheService;
	
	@Autowired
	private CustomerService customerService;
	
	/**
	 * used to list all the tag Types
	 * @return
	 */
	
	@RequestMapping(value = "/listAll", method = RequestMethod.GET)
	public @ResponseBody Iterable<TagType> listAll() {
		return tagTypeService.findAll();
	}
	
	/**
	 * 
	 * @param cid
	 * @return
	 */
	
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public JSONArray list(@RequestParam("cid") String cid) {
		List<TagType> tagTypeList = tagTypeService.findByCid(cid);
		if (CollectionUtils.isEmpty(tagTypeList)) {
			tagTypeList = tagTypeService.getDefaultTagTypeListAttributes();
		}

		JSONArray tagTypeArray = new JSONArray();

		tagTypeList.parallelStream().forEach(tagType -> {
			JSONObject object = new JSONObject();
			object.put("tagIcon", tagType.getTagIcon());
			object.put("tagIconColor", tagType.getTagIconColor());
			object.put("tagType", tagType.getTagType());
			tagTypeArray.add(object);
		});

		return tagTypeArray;
	}

	/**
	 * 
	 * @param tagType
	 * @param cid
	 * @return
	 */
	
	@RequestMapping(value = "/getTagType", method = RequestMethod.GET)
    public  List<TagType> tagType(@RequestParam("tagType") String tagType,
    							  @RequestParam("cid") String cid) {
		List<TagType> tagTypeList = tagTypeService.findByTagTypeAndCid(tagType,cid);
		if (CollectionUtils.isEmpty(tagTypeList)) {
			return tagTypeCacheService.getDefaultgCustomerTagTypeAttributes();
		}
		return tagTypeList;
    }
	
	/**
	 * Used to delete all the TagType
	 * @return
	 */
	
	@RequestMapping(value = "/deleteAll", method = RequestMethod.POST)
    public  boolean deleteAll() {
		tagTypeService.deleteAll();
		return true;
    }
 
	/**
	 * Used to delete tagType
	 * @param id
	 * @param request
	 * @return
	 */
	
	@RequestMapping(value = "/tagTypeDelete", method = RequestMethod.POST)
	public Restponse<String> ibeacondelete(@RequestParam("id") String id,HttpServletRequest request) {

		int code 		= 200;
		boolean success = true;
		String body 	= "Successfully deleted TagType";
		
		try {
			
			TagType tagType = tagTypeService.findOne(id);
			
			if (tagType != null) {
				tagTypeService.delete(tagType);
			} else {
				code 	= 404;
				success = false;
				body 	= "TagType not found";
			}
			
			
		} catch (Exception e) {
			success = false;
			code 	= 500;
			body 	= "an error occurred while deleting TagType " +e.getMessage();
		}
		
		return new Restponse<String>(success, code,body);
	}
	
	/**
	 * Used to save the tagType
	 * @param newTagType
	 * @param request
	 * @return
	 */
	
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public Restponse<String> save(@RequestBody TagType newTagType,HttpServletRequest request) {
		
		logger.info("Payload " + newTagType.toString());
		
		boolean success = true;
		int code 		= 200;
		String body 	= "TagType has been saved successfully.";
		
		try {

			if (StringUtils.isEmpty(newTagType.getCid()) 
					|| StringUtils.isEmpty(newTagType.getTagType())
					|| StringUtils.isEmpty(newTagType.getTagIcon())
					|| StringUtils.isEmpty(newTagType.getTagIconColor())) {
				
				body 	= "Required mandatory fields";
				success = false;
				code 	= 404;
				
				return new Restponse<String>(success, code, body);
				
			} else {
				if (newTagType.getId() == null) {
					newTagType.setCreatedBy(SessionUtil.currentUser(request.getSession()));
					newTagType.setCreatedOn(new Date());
					newTagType.setStatus("active");
				} else {
					TagType oldTagType = tagTypeService.findOne(newTagType.getId());
					if (oldTagType != null) {
						oldTagType.setModifiedBy(SessionUtil.currentUser(request.getSession()));
						oldTagType.setModifiedOn(new Date());
						oldTagType.setTagIcon(newTagType.getTagIcon());
						oldTagType.setTagIconColor(newTagType.getTagIconColor());
						oldTagType.setTagType(newTagType.getTagType());
						newTagType = oldTagType;
					} else {
						body 	= "Given id tagType not found" + newTagType.getId();
						success = false;
						code 	= 404;
						return new Restponse<String>(success, code, body);
					}
				}
				newTagType = tagTypeService.save(newTagType);
			}
		} catch (Exception e) {
			e.printStackTrace();
			body 	= "while save TagType occure error." +e.getMessage();
			success = false;
			code 	= 500;
		}
		
		return new Restponse<String>(success, code, body);
	}
	
	@RequestMapping(value = "/duplicateTagType", method = RequestMethod.GET)
	public Restponse<String> duplicateTagType(	@RequestParam("tagType") String tagType,
									@RequestParam("cid") String cid,HttpServletRequest request) {
		
		String message 	= "new";
		boolean flag 	= false;
		int code		= 401;
		
		logger.info(" tagType " + tagType + " cid  " + cid);
		
		try {
			
			tagType = StringUtils.trimToEmpty(tagType);
				
				List<TagType> tagTypeList = tagTypeService.findByTagTypeAlreadyExists(cid,tagType);
				
				logger.info(" tagTypeList " + tagTypeList);
			
				if (tagTypeList != null && tagTypeList.size() > 0) {
					
					TagType fence 	= tagTypeList.get(0);
					String name 	= StringUtils.trimToEmpty(fence.getTagType());
					
					if (name.equalsIgnoreCase(tagType)) {
							message = "duplicate";
							flag 	= false;
							code    = 400;
					} else {
						flag 	= true;
						message = "new";
						code    = 200;
					}
				} else {
					flag 	= true;
					message = "new";
					code    = 200;
				}
		} catch (Exception e) {
			message = "Error " + e.getMessage();
			flag 	= false;
			code	= 500;
			e.printStackTrace();
		}
		
		return new Restponse<String>(flag, code, message);
	}
	
	/**
	 * Used to get list of TagType particular customer 
	 * @param cid
	 * @return
	 */
	
	@RequestMapping(value = "/getTagTypeFromCustomerCache", method = RequestMethod.GET)
	public ConcurrentHashMap<String,JSONObject> getTagTypeFromCustomerCache(@RequestParam("cid") String cid) {
		return TagTypeCacheService.tagTypeCache.get(cid);
	}
	
	/**
	 * Get tagIcon and TagColor from cache
	 * @param tagType
	 * @param cid
	 * @param request
	 * @return
	 */
	
	@RequestMapping(value = "/getTagTypeFromCache", method = RequestMethod.GET)
	public JSONObject getTagTypeFromCache(@RequestParam("tagType") String tagType,
			@RequestParam("cid") String cid,HttpServletRequest request) {
		return tagTypeCacheService.getCacheTagTypeAttributes(cid, tagType);
	}
	
	/**
	 * Used to clear TagType list form particular customer
	 * @param cid
	 * @return
	 */
	
	@RequestMapping(value = "/clearTagTypeCid", method = RequestMethod.GET)
	public ConcurrentHashMap<String,JSONObject> deleteTagTypeFromCache(@RequestParam("cid") String cid) {
		if (StringUtils.isNotBlank(cid) && TagTypeCacheService.tagTypeCache.containsKey(cid)) {
			tagTypeCacheService.deleteTagTypeFromCache(cid);
		} else {
			logger.info("TagType is empty");
		}
		
		return TagTypeCacheService.tagTypeCache.get(cid);
	}
	
	/**
	 * Used to clear all the tagType from cache
	 * @return
	 */
	
	@RequestMapping(value = "/clearAllTagTypeCache", method = RequestMethod.GET)
	public Collection<ConcurrentHashMap<String, JSONObject>> clearAllTagTypeCache() {
		TagTypeCacheService.tagTypeCache.clear();
		return TagTypeCacheService.tagTypeCache.values();
	}
	
	
	/**
	 * Used to refresh tagType for particular customer 
	 * @param cid
	 * @param request
	 * @return
	 */
	
	@RequestMapping(value = "/loadTagTypeForCustomer", method = RequestMethod.GET)
	public ConcurrentHashMap<String,JSONObject> loadTagTypeForCustomer(@RequestParam("cid") String cid,HttpServletRequest request) {
		List<Customer> customerList = customerService.findOneById(cid);
		if (CollectionUtils.isNotEmpty(customerList)) {
			tagTypeCacheService.loadTagTypes(customerList);
		} else {
			logger.info("Customer list is empty.");
		}
		return TagTypeCacheService.tagTypeCache.get(cid);
	}

}
