package de.hhn.se.embedded.zigbee.raumserver;

import javax.persistence.Id;


public class Room {
	
	@Id
	private String roomId;

	private String name;



	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}