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
import java.util.Objects;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Reference;

public class MolekularPathologieBefundMapper extends DiagnosticReportMapper<IhcReport> {

  private final IhcMapper ihcMapper;

  public MolekularPathologieBefundMapper(IhcMapper ihcMapper) {
    this.ihcMapper = Objects.requireNonNull(ihcMapper);
  }

  @Override
  protected String getPatientId(IhcReport item) {
    return item.getPatient().getId();
  }

  @Override
  protected String getId(IhcReport item) {
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
    return new Reference()
        .setReference(
            String.format(
                "Specimen?identifier=%s/sid/specimen-id|%s",
                this.fhirSystemBaseUrl, sourceItem.getSpecimen().getId()));
  }
}
