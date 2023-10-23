package com.start.helm.domain.helm;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Aggregation of sections of Helm Chart files affected by introducing a new dependency
 * <p>
 * Helm-Dependency-specific changes to the Helm Chart are collected here.
 */
@Getter
@Setter
@EqualsAndHashCode
public class HelmChartSlice {

	private List<Map<String, Object>> environmentEntries;

	private Map<String, String> defaultConfig;

	private Map<String, String> preferredChart;

	private Map<String, Object> valuesEntries;

	private Map<String, Object> initContainer;

	private Map<String, Object> secretEntries;

}
