package de.hhn.se.embedded.zigbee.raumserver;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;

import de.hhn.se.embedded.zigbee.raumserver.web.UserService;
import de.hhn.se.embedded.zigbee.raumserver.zigbee.ZigBeeDevice;
import de.hhn.se.embedded.zigbee.raumserver.zigbee.ZigBeeDeviceImpl;

@RestController
@SpringBootApplication
@EnableJpaRepositories(basePackages = "de.hhn.se.embedded.zigbee.raumserver.domain")
@EnableAsync
@EnableScheduling
public class Application {

	@Value("${roomserver.id}")
	String queueName;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Bean
	Queue queue() {
		return new Queue(queueName, false);
	}

	@Bean
	TopicExchange exchange() {
		return new TopicExchange("ha-exchange");
	}

	@Bean
	Binding binding(Queue queue, TopicExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with(queueName);
	}

	@Bean
	SimpleMessageListenerContainer container(
			ConnectionFactory connectionFactory,
			MessageListenerAdapter listenerAdapter) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueName);
		container.setMessageListener(listenerAdapter);
		return container;
	}

	@Bean
	CommandHandler receiver() {
		return new CommandHandler();
	}

	@Bean
	RabbitAdmin admin(ConnectionFactory connectionFactory) {
		return new RabbitAdmin(connectionFactory);
	}

	@Bean
	MessageListenerAdapter listenerAdapter(CommandHandler receiver) {
		return new MessageListenerAdapter(receiver, "handleMessage");
	}

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	TemperatureController temperatureController() {
		return new DummyTemperatureController();
	}
	
	@Bean
	ZigBeeDevice zigBeeDevice(){
		return new ZigBeeDeviceImpl();
	}

	@Bean
	TemperatureSensor temperatureSensor() {
		try {
//			return new DummyTemperatureSensor();
			return new TemperatureSensorImpl();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Bean
	public InitializingBean insertDefaultUsers() {
		return new InitializingBean() {
			@Autowired
			private UserService userService;

			@Override
			public void afterPropertiesSet() {
				addUser("user", "user");

			}

			private void addUser(String username, String password) {
				this.userService.registerUserAndRoom(username, password);

			}
		};
	}

}