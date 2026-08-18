package se.sundsvall.messaging.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;

@Configuration
class EventPublisherConfiguration {

	/**
	 * The event multicaster runs on its own executor, which Spring Boot's task execution auto-configuration never
	 * touches. The MDC/Identifier-propagating {@link TaskDecorator} provided by dept44 therefore has to be applied to it
	 * explicitly, or events lose the request id and the sender identity of the request that published them.
	 */
	@Bean(name = "applicationEventMulticaster")
	ApplicationEventMulticaster simpleApplicationEventMulticaster(final TaskDecorator taskDecorator) {
		var eventMulticaster = new SimpleApplicationEventMulticaster();
		var taskExecutor = new SimpleAsyncTaskExecutor();
		taskExecutor.setTaskDecorator(taskDecorator);
		eventMulticaster.setTaskExecutor(taskExecutor);
		return eventMulticaster;
	}
}
