package com.start.helm.domain.maven;

import com.start.helm.domain.dependency.DependencyFetcher;
import com.start.helm.domain.helm.HelmContext;
import com.start.helm.domain.maven.resolvers.DependencyResoler;
import com.start.helm.domain.maven.resolvers.SpringBootStarterAmqpResolver;
import com.start.helm.domain.maven.resolvers.SpringBootStarterWebResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.api.model.Dependency;
import org.apache.maven.api.model.Model;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MavenModelProcessor {

  private final DependencyFetcher dependencyFetcher;

  private final List<DependencyResoler> dependencyMatchers = new ArrayList<>();

  public MavenModelProcessor(DependencyFetcher dependencyFetcher) {
    this.dependencyFetcher = dependencyFetcher;
    this.dependencyMatchers.addAll(List.of(
        new SpringBootStarterWebResolver(),
        new SpringBootStarterAmqpResolver()
    ));
  }

  public HelmContext process(Model m) {
    List<Dependency> dependencies = m.getDependencies();

    HelmContext context = new HelmContext();

    context.setAppName(m.getArtifactId());
    context.setAppVersion(m.getVersion());

    dependencies.stream()
        .filter(d -> !"test".equals(d.getScope()))
        .map(d -> dependencyMatchers
            .stream()
            .filter(matcher -> matcher.matches(d.getArtifactId()))
            .findFirst()
        )
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(d -> d.resolveDependency(context))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet())
        .forEach(context::addHelmChartFragment);

    log.info("Helm context: {}", context);
    return context;
  }


}
