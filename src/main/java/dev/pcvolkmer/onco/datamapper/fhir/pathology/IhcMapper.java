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
import dev.pcvolkmer.mv64e.model.ProteinExpression;
import dev.pcvolkmer.onco.datamapper.fhir.ManyMapper;
import dev.pcvolkmer.onco.datamapper.fhir.ObservationMapper;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.*;

public class IhcMapper extends ObservationMapper<ProteinExpression>
    implements ManyMapper<IhcReport, Observation> {

  // TODO: Unterscheidung PDL1 / HER2 / generisch
  // FIXME: vermutlich falsche Testdaten?
  //  TPS/CPS/ICS/TC-Scores auch bei nicht PDL1 Genen?
  //  1+/2+/3+ Codes außerhalb von HER2?

  @Override
  protected String getPatientId(ProteinExpression item) {
    return item.getPatient().getId();
  }

  @Override
  protected String getId(ProteinExpression item) {
    return String.format("%s_ihc", item.getId());
  }

  @Override
  public Observation map(ProteinExpression sourceItem) {
    var result = new Observation();

    result.addIdentifier().setSystem(this.getSystem()).setValue(this.getId(sourceItem));

    result.setMeta(
        new Meta()
            .setSource(this.fhirMetaSource)
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-immunohistochemistry"));

    result.setStatus(Observation.ObservationStatus.FINAL);

    result
        .addCategory()
        .addCoding()
        .setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
        .setCode("laboratory")
        .setDisplay("Laboratory");

    result
        .addCategory()
        .addCoding()
        .setSystem("http://hl7.org/fhir/uv/genomics-reporting/CodeSystem/tbd-codes-cs")
        .setCode("biomarker-category");

    result.setCode(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("1234806008")
                    .setDisplay("Observation using immunohistochemistry (observable entity)")));

    result.setValue(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem(sourceItem.getValue().getSystem())
                    .setCode(sourceItem.getValue().getCode().getValue())
                    .setDisplay(sourceItem.getValue().getDisplay())));

    // Gene
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setSystem("http://loinc.org")
                            .setCode("48018-6")
                            .setDisplay("Gene studied [ID]")))
            .setValue(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setSystem(sourceItem.getProtein().getSystem())
                            .setCode(sourceItem.getProtein().getCode())
                            .setDisplay(sourceItem.getProtein().getDisplay()))));

    result.setSubject(this.getPatientReference(sourceItem));

    return result;
  }

  @Override
  public List<Observation> mapToMany(IhcReport sourceItem) {
    return getProteinExpressions(sourceItem).stream().map(this::map).collect(Collectors.toList());
  }

  @Override
  public void addManyToBundle(Bundle bundle, IhcReport sourceItem) {
    getProteinExpressions(sourceItem).forEach(item -> this.addToBundle(bundle, item));
  }

  private List<ProteinExpression> getProteinExpressions(IhcReport sourceItem) {
    if (sourceItem.getResults() == null || sourceItem.getResults().getProteinExpression() == null) {
      return List.of();
    }

    return sourceItem.getResults().getProteinExpression().stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }
}
