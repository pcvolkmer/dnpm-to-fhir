package dev.pcvolkmer.onco.datamapper.fhir.builders;

import dev.pcvolkmer.onco.datamapper.fhir.DnpmToFhirMapper;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

public class DefaultReferenceBuilder implements ReferenceBuilder {

  @Override
  public <S, D extends Resource> Reference getPatientReference(
      S item, DnpmToFhirMapper<S, D> dnpmToFhirMapper) {
    return new Reference()
        .setReference(String.format("Patient/%s", dnpmToFhirMapper.getPatientId(item)));
  }

  @Override
  public <S, D extends Resource> Reference getPatientReference(
      String id, DnpmToFhirMapper<S, D> dnpmToFhirMapper) {
    return new Reference().setReference(String.format("Patient/%s", id));
  }

  @Override
  public <S, D extends Resource> Reference getReference(
      S item, DnpmToFhirMapper<S, D> dnpmToFhirMapper) {
    return new Reference()
        .setReference(
            String.format(
                "%s/%s", dnpmToFhirMapper.getFhirResourceType(), dnpmToFhirMapper.getId(item)));
  }

  @Override
  public <S, D extends Resource> Reference getReference(
      String id, DnpmToFhirMapper<S, D> dnpmToFhirMapper) {
    return new Reference()
        .setReference(String.format("%s/%s", dnpmToFhirMapper.getFhirResourceType(), id));
  }
}
