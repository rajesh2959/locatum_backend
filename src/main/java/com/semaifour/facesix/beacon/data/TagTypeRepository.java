package com.semaifour.facesix.beacon.data;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * 
 * @author Qubercomm Inc
 * created on 2019/04/24
 *
 */

public interface TagTypeRepository extends MongoRepository<TagType, String> {
	
	public TagType findById(String id);
	
	@Query("{tagType:?0}")
	public List<TagType> findBytagType(String tagType);
	
	@Query("{cid:?0}")
	public List<TagType> findByCid(String cid);

	@Query("{tagType:?0,cid:?1}")
	public List<TagType> findByTagTypeAndCid(String tagType, String cid);

	@Query("{cid:?0,tagType: {$regex : '^?1$', $options: 'i'}}")
	public List<TagType> findByTagTypeAlreadyExists(String cid, String tagType);
	
}

