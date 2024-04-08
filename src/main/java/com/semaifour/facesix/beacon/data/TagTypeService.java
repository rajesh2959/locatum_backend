package com.semaifour.facesix.beacon.data;

import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import net.sf.json.JSONObject;

/**
 * 
 * @author Qubercomm Inc
 *  created on 2019/04/24
 *
 */
@Service
public class TagTypeService {

	private static Logger LOG = LoggerFactory.getLogger(TagTypeService.class.getName());

	@Autowired(required = false)
	private TagTypeRepository repository;

	@Autowired
	private TagTypeCacheService tagTypeCacheService;
	
	public TagType save(TagType tagType) {
		tagType = repository.save(tagType);
		if (tagType.getPkid() == null) {
			tagType.setPkid(tagType.getId());
			tagType = repository.save(tagType);
		}
		
		LOG.info("saved sucessfully " +tagType.getId());
		
		String cid = tagType.getCid();
		tagTypeCacheService.updateCacheTagAttributes(cid, Arrays.asList(tagType));
		
		return tagType;
	}

	public TagType findOne(String id) {
		return repository.findOne(id);
	}

	public List<TagType> findByCid(String cid) {
		return repository.findByCid(cid);
	}

	public List<TagType> findByTagType(String type) {
		return repository.findBytagType(type);
	}

	public List<TagType> findAll() {
		return repository.findAll();
	}

	public void delete(TagType beacon) {
		repository.delete(beacon);
	}

	public void deleteAll() {
		repository.deleteAll();
	}

	public List<TagType> findByTagTypeAlreadyExists(String cid,String tagType) {
		return repository.findByTagTypeAlreadyExists(cid,tagType);
	}
	public List<TagType> findByTagTypeAndCid(String tagType, String cid) {
		return repository.findByTagTypeAndCid(tagType,cid);
	}
	
	public List<TagType> getDefaultTagTypeListAttributes() {
		return tagTypeCacheService.getDefaultgCustomerTagTypeAttributes();
	}
	
	public JSONObject getDefaultJSONTagTypeAttributes() {
		return tagTypeCacheService.getDefaultCacheTagTypeAttributes();
	}

	public List<TagType> save(List<TagType> defaultTagTypes) {
		return repository.save(defaultTagTypes);
		
	}

}
