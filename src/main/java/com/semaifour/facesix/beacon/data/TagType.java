package com.semaifour.facesix.beacon.data;


import org.springframework.data.annotation.Id;
import com.semaifour.facesix.domain.FSObject;

/**
 * 
 * @author Qubercomm Inc
 * created on 2019/04/24
 *
 */
public class TagType extends FSObject {

	@Id
	private String id;
	private String	cid;
	private String tagType;
	private String tagIcon;
	private String tagIconColor;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getTagType() {
		return tagType;
	}
	public void setTagType(String tagType) {
		this.tagType = tagType;
	}
	public String getTagIcon() {
		return tagIcon;
	}
	public void setTagIcon(String tagIcon) {
		this.tagIcon = tagIcon;
	}
	public String getTagIconColor() {
		return tagIconColor;
	}
	public void setTagIconColor(String tagIconColor) {
		this.tagIconColor = tagIconColor;
	}
	@Override
	public String toString() {
		return "TagType [id=" + id + ", cid=" + cid + ", tagType=" + tagType + ", tagIcon=" + tagIcon
				+ ", tagIconColor=" + tagIconColor + "]";
	}
	
}
