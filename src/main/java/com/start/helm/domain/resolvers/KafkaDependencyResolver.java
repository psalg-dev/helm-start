package com.start.helm.domain.resolvers;

import com.start.helm.domain.helm.HelmContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.start.helm.util.HelmUtil.makeSecretKeyRef;

/**
 * Resolver for spring kafka dependency.
 */
@Slf4j
@Component
public class KafkaDependencyResolver implements DependencyResolver {

	//@formatter:off

	@Override
	public String dependencyName() {
		return "kafka";
	}

	@Override
	public List<String> matchOn() {
		return List.of("kafka-streams", "spring-cloud-starter-stream-kafka", "spring-kafka");
	}

	public Map<String, Object> getSecretEntries() {
		return Map.of(
				"kafka-username", "{{ .Values.kafka.auth.username | b64enc | quote }}"
				, "kafka-password", "{{ .Values.kafka.auth.password | b64enc | quote }}"
		);
	}

	public Map<String, Object> getValuesEntries(HelmContext context) {
		return Map.of(
				"kafka",
				Map.of("enabled", true,
						"port", 5672,
						"vhost", "/",
						"nameOverride", context.getAppName() + "-kafka",
						"fullnameOverride", context.getAppName() + "-kafka",
						"auth", Map.of("username", "guest", "password", "guest")
				),
				"global", Map.of("hosts", Map.of("kafka", context.getAppName() + "-kafka"), "ports", Map.of("kafka", 5672))
		);
	}

	public Map<String, String> getPreferredChart() {
		return Map.of(
				"name", "kafka",
				"version", "26.4.2",
				"repository", "https://charts.bitnami.com/bitnami"
		);
	}

	public Map<String, String> getDefaultConfig() {
		return Map.of(
				"spring.kafka.host", "{{ .Values.global.hosts.kafka }}",
				"spring.kafka.port", "{{ .Values.global.ports.kafka }}",
				"spring.kafka.virtual-host", "{{ .Values.kafka.vhost }}"
		);
	}

	public List<Map<String, Object>> getEnvironmentEntries(HelmContext context) {
		return List.of(
				makeSecretKeyRef("SPRING_KAFKA_USERNAME", "kafka-username", context.getAppName()),
				makeSecretKeyRef("SPRING_KAFKA_PASSWORD", "kafka-password", context.getAppName())
		);
	}

}
