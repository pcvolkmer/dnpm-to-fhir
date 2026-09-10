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

package dev.pcvolkmer.onco.datamapper.fhir.careplan;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import dev.pcvolkmer.mv64e.model.MtbMedicationRecommendation;
import dev.pcvolkmer.onco.datamapper.fhir.MedicationRequestMapper;
import dev.pcvolkmer.onco.datamapper.fhir.builders.ReferenceBuilder;
import dev.pcvolkmer.onco.datamapper.fhir.diagnosis.MtbDiagnoseMapper;
import dev.pcvolkmer.onco.datamapper.fhir.filter.AtcCodeFilter;
import dev.pcvolkmer.onco.datamapper.fhir.filter.Filter;
import java.util.List;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Type;

public class TherapieempfehlungMapper extends MedicationRequestMapper<MtbMedicationRecommendation> {

  public TherapieempfehlungMapper(
      ReferenceBuilder referenceBuilder, List<Filter<? extends Type>> filters) {
    super(referenceBuilder, filters);
  }

  @Override
  public String getPatientId(MtbMedicationRecommendation item) {
    return item.getPatient().getId();
  }

  @Override
  public String getId(MtbMedicationRecommendation item) {
    return String.format("%s_medicationrequest", item.getId());
  }

  @Override
  public MedicationRequest map(MtbMedicationRecommendation sourceItem) {
    var result = new MedicationRequest();
    result.addIdentifier().setSystem(this.getSystem()).setValue(this.getId(sourceItem));

    result.setMeta(
        new Meta()
            .setSource(this.fhirMetaSource)
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-therapieempfehlung"));

    final var levelOfEvidence = sourceItem.getLevelOfEvidence();

    if (null != levelOfEvidence && null != levelOfEvidence.getGrading()) {
      final var evidenzlevelExtension =
          new Extension()
              .setUrl(
                  "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-ex-mtb-empfehlung-evidenzgraduierung");
      final var evidenzlevelValue =
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setCode(levelOfEvidence.getGrading().getCode().getValue())
                      .setSystem(
                          "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/CodeSystem/mii-cs-mtb-empfehlung-evidenzgrad"));

      final var addendums = levelOfEvidence.getAddendums();
      if (null != addendums) {
        addendums.forEach(
            addendum ->
                evidenzlevelValue.addCoding(
                    new Coding()
                        .setCode(addendum.getCode().getValue())
                        .setSystem(
                            "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/CodeSystem/mii-cs-mtb-empfehlung-evidenzgrad-zusatzverweis")));
      }

      evidenzlevelExtension.setValue(evidenzlevelValue);

      result.addExtension(evidenzlevelExtension);
    }

    // Current active care plan? No Information in DNPM - but required!
    // TODO: Lookup in FollowUp mit Status Therapieumsetzung
    result.setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE);
    result.setIntent(MedicationRequest.MedicationRequestIntent.PROPOSAL);

    final var dateValue = new DateTimeType();
    dateValue.setValue(sourceItem.getIssuedOn());
    dateValue.setPrecision(TemporalPrecisionEnum.DAY);
    result.setAuthoredOnElement(dateValue);

    final var reason = sourceItem.getReason();
    if (null != reason) {
      final var reasonReference =
          new Reference()
              .setReference(
                  this.referenceBuilder
                      .getReference(
                          reason.getId() + "_mtbdiagnose",
                          new MtbDiagnoseMapper(this.referenceBuilder, this.filters))
                      .getReference());
      result.addReasonReference(reasonReference);
    }

    final var atcCodeFilter =
        this.filters.stream()
            .filter(AtcCodeFilter.class::isInstance)
            .map(AtcCodeFilter.class::cast)
            .findAny();
    final var medication = new CodeableConcept();
    if (null != sourceItem.getMedication()) {
      sourceItem
          .getMedication()
          .forEach(
              medCoding -> {
                final var coding =
                    new Coding()
                        .setSystem(medCoding.getSystem().getValue())
                        .setCode(medCoding.getCode())
                        .setDisplay(medCoding.getDisplay());

                if (atcCodeFilter.isPresent()) {
                  if (atcCodeFilter.get().matchesRequirement(coding)) {
                    medication.addCoding(coding);
                  }
                } else {
                  medication.addCoding(coding);
                }
              });
    }

    result.setMedication(medication);

    result.setSubject(this.getPatientReference(sourceItem));

    return result;
  }
}
