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

package dev.pcvolkmer.onco.datamapper.fhir.pathology;

import dev.pcvolkmer.mv64e.model.IhcReport;
import dev.pcvolkmer.onco.datamapper.fhir.DiagnosticReportMapper;
import dev.pcvolkmer.onco.datamapper.fhir.SpecimenMapper;
import dev.pcvolkmer.onco.datamapper.fhir.builders.ReferenceBuilder;
import dev.pcvolkmer.onco.datamapper.fhir.filter.Filter;
import java.util.List;
import java.util.Objects;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Type;

public class MolekularPathologieBefundMapper extends DiagnosticReportMapper<IhcReport> {

  private final IhcMapper ihcMapper;

  public MolekularPathologieBefundMapper(
      ReferenceBuilder referenceBuilder,
      IhcMapper ihcMapper,
      List<Filter<? extends Type>> filters) {
    super(referenceBuilder, filters);
    this.ihcMapper = Objects.requireNonNull(ihcMapper);
  }

  @Override
  public String getPatientId(IhcReport item) {
    return item.getPatient().getId();
  }

  @Override
  public String getId(IhcReport item) {
    return String.format("%s_molecular-pathology-report", item.getId());
  }

  @Override
  public DiagnosticReport map(IhcReport sourceItem) {
    var result = new DiagnosticReport();

    result.addIdentifier().setSystem(this.getSystem()).setValue(this.getId(sourceItem));

    result.setMeta(
        new Meta()
            .setSource(this.fhirMetaSource)
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-molecular-pathology-report"));

    result.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);

    result.setCode(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem("http://loinc.org")
                    .setCode("60568-3")
                    .setDisplay("Pathology synoptic report")));

    result.setSubject(this.getPatientReference(sourceItem));

    if (sourceItem.getIssuedOn() != null) {
      result.setIssued(sourceItem.getIssuedOn());
    }

    if (sourceItem.getSpecimen() != null) {
      result.addSpecimen(this.getSpecimenReference(sourceItem));
    }

    this.ihcMapper.getReferences(sourceItem).forEach(result::addResult);

    return result;
  }

  private Reference getSpecimenReference(IhcReport sourceItem) {
    return this.referenceBuilder.getReference(
        sourceItem.getSpecimen().getId(), new SpecimenMapper(this.referenceBuilder, filters));
  }
}
