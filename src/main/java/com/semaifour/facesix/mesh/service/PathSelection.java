package com.semaifour.facesix.mesh.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PathSelection {

	private String  opcode;
	private String  uid;
	private String  dst;
	private String  next_hop;
	private int 	score;
	private String  prev_hop;
	private int     prev_score;
	private int  	num_hops;
	private int 	prev_num_hops;
	
	private String timestamp;
	private long lastSeen;
	
	public String getOpcode() {
		return opcode;
	}
	public void setOpcode(String opcode) {
		this.opcode = opcode;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getDst() {
		return dst;
	}
	public void setDst(String dst) {
		this.dst = dst;
	}
	public String getNext_hop() {
		return next_hop;
	}
	public void setNext_hop(String next_hop) {
		this.next_hop = next_hop;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public long getLastSeen() {
		return lastSeen;
	}
	public void setLastSeen(long lastSeen) {
		this.lastSeen = lastSeen;
	}
	public String getPrev_hop() {
		return prev_hop;
	}
	public void setPrev_hop(String prev_hop) {
		this.prev_hop = prev_hop;
	}
	public int getPrev_score() {
		return prev_score;
	}
	public void setPrev_score(int prev_score) {
		this.prev_score = prev_score;
	}
	
	public int getNum_hops() {
		return num_hops;
	}
	public void setNum_hops(int num_hops) {
		this.num_hops = num_hops;
	}
	public int getPrev_num_hops() {
		return prev_num_hops;
	}
	public void setPrev_num_hops(int prev_num_hops) {
		this.prev_num_hops = prev_num_hops;
	}
	@Override
	public String toString() {
		return "PathSelection [opcode=" + opcode + ", uid=" + uid + ", dst=" + dst + ", next_hop=" + next_hop
				+ ", score=" + score + ", prev_hop=" + prev_hop + ", prev_score=" + prev_score + ", num_hops="
				+ num_hops + ", prev_num_hops=" + prev_num_hops + ", timestamp=" + timestamp + ", lastSeen=" + lastSeen
				+ "]";
	}
	
}
