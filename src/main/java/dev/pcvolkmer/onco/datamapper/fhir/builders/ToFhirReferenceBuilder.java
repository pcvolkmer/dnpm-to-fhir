package dev.pcvolkmer.onco.datamapper.fhir.builders;

import dev.pcvolkmer.onco.datamapper.fhir.DnpmToFhirMapper;
import dev.pcvolkmer.onco.datamapper.fhir.FhirProperties;
import io.github.dizuker.tofhir.IdUtils;
import java.util.Objects;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

public class ToFhirReferenceBuilder implements ReferenceBuilder {

  private final FhirProperties fhirProperties;
  private final String fhirServerBaseUrl;

  public ToFhirReferenceBuilder(FhirProperties fhirProperties, String fhirServerBaseUrl) {

    this.fhirProperties = Objects.requireNonNull(fhirProperties);
    this.fhirServerBaseUrl = Objects.requireNonNull(fhirServerBaseUrl).replaceAll("/+$", "");

    if (this.fhirServerBaseUrl.isBlank()) {
      throw new IllegalArgumentException("FHIR server base URL must not be empty");
    }
  }

  @Override
  public FhirProperties getFhirProperties() {
    return this.fhirProperties;
  }

  @Override
  public void setResourceId(Resource resource, Identifier identifier) {
    resource.setId(IdUtils.fromIdentifier(identifier));
  }

  @Override
  public void configureBundleEntry(Bundle.BundleEntryComponent entry) {

    var resource = entry.getResource();

    if (resource == null || !resource.hasId()) {
      throw new IllegalStateException("Resource must have a deterministic ID");
    }

    String requestUrl = resource.fhirType() + "/" + resource.getIdElement().getIdPart();

    entry.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl(requestUrl);

    entry.setFullUrl(fhirServerBaseUrl + "/" + requestUrl);
  }

  @Override
  public <S, D extends Resource> Reference getConditionalPatientReference(
      String id, DnpmToFhirMapper<S, D> mapper) {

    return this.getPatientReference(id, mapper);
  }

  @Override
  public <S, D extends Resource> Reference getPatientReference(
      S item, DnpmToFhirMapper<S, D> dnpmToFhirMapper) {

    return getPatientReference(dnpmToFhirMapper.getPatientId(item), dnpmToFhirMapper);
  }

  @Override
  public <S, D extends Resource> Reference getPatientReference(
      String id, DnpmToFhirMapper<S, D> dnpmToFhirMapper) {

    return createReference("Patient", fhirProperties.identifierSystem("patient-id"), id);
  }

  @Override
  public <S, D extends Resource> Reference getReference(
      S item, DnpmToFhirMapper<S, D> dnpmToFhirMapper) {

    return getReference(dnpmToFhirMapper.getId(item), dnpmToFhirMapper);
  }

  @Override
  public <S, D extends Resource> Reference getReference(
      String id, DnpmToFhirMapper<S, D> dnpmToFhirMapper) {

    return createReference(
        dnpmToFhirMapper.getFhirResourceType(), dnpmToFhirMapper.getSystem(), id);
  }

  private Reference createReference(String resourceType, String system, String value) {

    var identifier = new Identifier().setSystem(system).setValue(value);

    var id = IdUtils.fromIdentifier(identifier).getIdPart();

    return new Reference(String.format("%s/%s", resourceType, id));
  }
}
