package de.hhn.se.embedded.zigbee.raumserver.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface DeviceRepository extends JpaRepository<Device, String> {
	
	Device findByZigBeeAddress(String a);
	
	List<Device> findByType(String t);
	

}