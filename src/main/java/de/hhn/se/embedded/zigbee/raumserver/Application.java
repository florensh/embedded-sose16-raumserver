package de.hhn.se.embedded.zigbee.raumserver;

import java.util.concurrent.Future;

import javax.annotation.PreDestroy;

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
import org.springframework.boot.actuate.system.ApplicationPidFileWriter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import de.hhn.se.embedded.zigbee.raumserver.web.UserService;

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

	// public static void main(String[] args) throws InterruptedException {
	// SpringApplication.run(Application.class, args);
	// }

	static Future ledFuture;

	static GpioPinDigitalOutput led;

	public static void main(String[] args) {

		ConfigurableApplicationContext ctx = null;

		final GpioController gpio = GpioFactory.getInstance();
		// provision gpio pins #01 as an output pin and make sure is is set
		// to
		// LOW at startup
		led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, // PIN NUMBER
				"My LED", // PIN FRIENDLY NAME (optional)
				PinState.LOW); // PIN STARTUP STATE (optional)

		ledFuture = led.blink(1000, PinState.HIGH);

		SpringApplication springApplication = new SpringApplication(
				Application.class);

		springApplication.addListeners(new ApplicationPidFileWriter(
				"RoomServer.pid"));
		ctx = springApplication.run(args);
		ctx.registerShutdownHook();

	}

	@Bean
	public ApplicationListener stopLEDBean() {
		return new ApplicationListener() {

			@PreDestroy
			private void setLedOff() {
				if(led != null){
					led.setState(PinState.LOW);
					
				}
			}

			@Override
			public void onApplicationEvent(ApplicationEvent event) {
				if(ledFuture != null && led != null){
					ledFuture.cancel(true);
					led.setState(PinState.HIGH);
					
				}

			}
		};

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