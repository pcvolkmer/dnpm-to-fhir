package dev.pcvolkmer.onco.datamapper.fhir.filter;

import dev.pcvolkmer.mv64e.model.AtcUnregisteredMedicationCoding;
import org.hl7.fhir.r4.model.Coding;

public class AtcCodeFilter implements CodingFilter {

  @Override
  public boolean matchesRequirement(Coding item) {
    return null != item
        && AtcUnregisteredMedicationCoding.SystemEnum.HTTP_FHIR_DE_CODE_SYSTEM_BFARM_ATC
            .getValue()
            .equals(item.getSystem());
  }
}
