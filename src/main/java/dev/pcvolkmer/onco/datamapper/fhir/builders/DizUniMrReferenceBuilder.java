package dev.pcvolkmer.onco.datamapper.fhir.builders;

import dev.pcvolkmer.onco.datamapper.fhir.DnpmToFhirMapper;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

public class DizUniMrReferenceBuilder implements ReferenceBuilder {

  @Override
  public <S, D extends Resource> Reference getPatientReference(
      S item, DnpmToFhirMapper<S, D> dnpmToFhirMapper) {
    String fhirSystemBaseUrl = "https://fhir.diz.uni-marburg.de";
    return new Reference()
        .setReference(
            String.format(
                "Patient?identifier=%s/sid/patient-id|%s",
                fhirSystemBaseUrl, dnpmToFhirMapper.getPatientId(item)));
  }

  @Override
  public <S, D extends Resource> Reference getPatientReference(
      String id, DnpmToFhirMapper<S, D> dnpmToFhirMapper) {
    String fhirSystemBaseUrl = "https://fhir.diz.uni-marburg.de";
    return new Reference()
        .setReference(
            String.format("Patient?identifier=%s/sid/patient-id|%s", fhirSystemBaseUrl, id));
  }

  @Override
  public <S, D extends Resource> Reference getReference(
      S item, DnpmToFhirMapper<S, D> dnpmToFhirMapper) {
    return new Reference()
        .setReference(
            String.format(
                "%s?identifier=%s|%s",
                dnpmToFhirMapper.getFhirResourceType(),
                dnpmToFhirMapper.getSystem(),
                dnpmToFhirMapper.getId(item)));
  }

  @Override
  public <S, D extends Resource> Reference getReference(
      String id, DnpmToFhirMapper<S, D> dnpmToFhirMapper) {
    return new Reference()
        .setReference(
            String.format(
                "%s?identifier=%s|%s",
                dnpmToFhirMapper.getFhirResourceType(), dnpmToFhirMapper.getSystem(), id));
  }
}
