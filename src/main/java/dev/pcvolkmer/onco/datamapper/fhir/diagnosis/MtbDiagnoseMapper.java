/*
 * This file is part of mv64e-onkostar-data
 *
 * Copyright (C) 2026 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.pcvolkmer.onco.datamapper.fhir.diagnosis;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import dev.pcvolkmer.mv64e.model.MtbDiagnosis;
import dev.pcvolkmer.onco.datamapper.fhir.ConditionMapper;
import dev.pcvolkmer.onco.datamapper.fhir.builders.ReferenceBuilder;
import dev.pcvolkmer.onco.datamapper.fhir.filter.Filter;
import java.util.List;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Type;

public class MtbDiagnoseMapper extends ConditionMapper<MtbDiagnosis> {

  public MtbDiagnoseMapper(
      ReferenceBuilder referenceBuilder, List<Filter<? extends Type>> filters) {
    super(referenceBuilder, filters);
  }

  @Override
  public String getPatientId(MtbDiagnosis item) {
    return item.getPatient().getId();
  }

  @Override
  public String getId(MtbDiagnosis item) {
    return String.format("%s_mtbdiagnose", item.getId());
  }

  @Override
  public Condition map(MtbDiagnosis diagnose) {
    var condition = new Condition();
    condition.addIdentifier().setSystem(this.getSystem()).setValue(diagnose.getId());

    condition.setMeta(
        new Meta()
            .setSource(this.fhirMetaSource)
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-diagnose-primaertumor"));

    condition.setCode(
        new CodeableConcept()
            .setCoding(
                List.of(
                    new Coding()
                        .setSystem(diagnose.getCode().getSystem())
                        .setVersion(diagnose.getCode().getVersion())
                        .setCode(diagnose.getCode().getCode())
                        .setDisplay(diagnose.getCode().getDisplay()))));

    condition.setSubject(this.referenceBuilder.getPatientReference(diagnose, this));

    if (null != diagnose.getRecordedOn()) {
      final var dateTimeType = new DateTimeType(diagnose.getRecordedOn());
      dateTimeType.setPrecision(TemporalPrecisionEnum.DAY);
      condition.setRecordedDateElement(dateTimeType);

      condition.setExtension(
          List.of(
              new Extension()
                  .setUrl("http://hl7.org/fhir/StructureDefinition/condition-assertedDate")
                  .setValue(dateTimeType)));
    }

    return condition;
  }
}
