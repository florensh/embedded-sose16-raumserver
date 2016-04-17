package de.hhn.se.embedded.zigbee.raumserver.domain;

import org.springframework.data.repository.CrudRepository;

public interface ConfigurationRepository extends CrudRepository<Configuration, Long>{
	
	Configuration findByParamName(String paramName);

}
