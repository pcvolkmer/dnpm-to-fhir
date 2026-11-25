package dev.pcvolkmer.onco.datamapper.fhir;

import java.util.Map;
import java.util.Objects;

public record FhirProperties(String identifierBaseUrl, String metaSource, Systems systems) {

  public FhirProperties {
    Objects.requireNonNull(identifierBaseUrl);
    Objects.requireNonNull(metaSource);
    Objects.requireNonNull(systems);

    identifierBaseUrl = identifierBaseUrl.strip().replaceAll("/+$", "");

    if (identifierBaseUrl.isBlank()) {
      throw new IllegalArgumentException("Identifier base URL must not be empty");
    }

    if (metaSource.isBlank()) {
      throw new IllegalArgumentException("Meta source must not be empty");
    }
  }

  public record Systems(Map<String, String> identifiers) {

    public Systems {
      identifiers = Map.copyOf(identifiers);
    }
  }

  public String identifierSystem(String name) {
    return systems.identifiers().getOrDefault(name, identifierBaseUrl + "/sid/" + name);
  }

  public static FhirProperties fromBaseUrl(String baseUrl) {

    return new FhirProperties(baseUrl, baseUrl + "/data-source/dnpm", new Systems(Map.of()));
  }

  public static FhirProperties marburg() {
    return fromBaseUrl("https://fhir.diz.uni-marburg.de");
  }
}
