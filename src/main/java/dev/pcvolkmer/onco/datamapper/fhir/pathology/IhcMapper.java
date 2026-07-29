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
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.*;

public class IhcMapper extends ObservationMapper<ProteinExpression>
    implements ManyMapper<IhcReport, Observation> {

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
    var profile = resolveProfile(sourceItem);

    result.addIdentifier().setSystem(this.getSystem()).setValue(this.getId(sourceItem));

    result.setMeta(new Meta().setSource(this.fhirMetaSource).addProfile(profile.getCanonicalUrl()));

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

    if (profile == IhcProfile.HER2) {
      result.setCode(
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setSystem("http://loinc.org")
                      .setCode("18474-7")
                      .setDisplay("HER2 Ag [Presence] in Tissue by Immune stain")));
    }

    // value or dataAbsentReason if unknown
    // TODO: oder ist "unknown" eher ein gültiges value?
    // Datenmodell sagt: "Untersucht, kein Ergebnis"
    if ("unknown".equals(sourceItem.getValue().getCode().getValue())) {
      result.setDataAbsentReason(
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setSystem("http://terminology.hl7.org/CodeSystem/data-absent-reason")
                      .setCode("unknown")
                      .setDisplay("Unknown")));
    } else if (profile == IhcProfile.HER2) {
      result.setValue(mapHer2ResultValue(sourceItem.getValue().getCode().getValue()));
    } else {
      result.setValue(
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setSystem(sourceItem.getValue().getSystem())
                      .setCode(sourceItem.getValue().getCode().getValue())
                      .setDisplay(sourceItem.getValue().getDisplay())));
    }

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

    if (profile == IhcProfile.PDL1) {
      addPdl1ScoreComponents(result, sourceItem);
    }

    result.setSubject(this.getPatientReference(sourceItem));

    return result;
  }

  private IhcProfile resolveProfile(ProteinExpression sourceItem) {
    if (sourceItem.getProtein() == null) {
      throw new IllegalArgumentException("IHC protein must not be null");
    }

    var proteinCode = sourceItem.getProtein().getCode();

    if ("HGNC:3430".equals(proteinCode)) {
      return IhcProfile.HER2;
    }

    if ("HGNC:17635".equals(proteinCode)) {
      return IhcProfile.PDL1;
    }

    return IhcProfile.GENERIC;
  }

  // using LL4396-9
  private CodeableConcept mapHer2ResultValue(String resultCode) {
    String loincCode;
    String display;

    switch (resultCode) {
      case "0":
        loincCode = "LA6111-4";
        display = "0";
        break;
      case "1+":
        loincCode = "LA11841-6";
        display = "1+";
        break;
      case "2+":
        loincCode = "LA11842-4";
        display = "2+";
        break;
      case "3+":
        loincCode = "LA11843-2";
        display = "3+";
        break;
      case "indeterminate":
        loincCode = "LA11884-6";
        display = "Indeterminate";
        break;
      default:
        throw new IllegalArgumentException(
            String.format("Result code %s not suported for HER2 IHC", resultCode));
    }

    return new CodeableConcept()
        .addCoding(
            new Coding().setSystem("http://loinc.org").setCode(loincCode).setDisplay(display));
  }

  private void addPdl1ScoreComponents(Observation target, ProteinExpression sourceItem) {

    if (sourceItem.getTpsScore() != null) {
      target.addComponent(
          new Observation.ObservationComponentComponent()
              .setCode(
                  new CodeableConcept()
                      .addCoding(
                          new Coding()
                              .setSystem(
                                  "https://nih-ncpi.github.io/ncpi-fhir-ig/CodeSystem-ncit.html")
                              .setCode("C184941")
                              .setDisplay("PD-L1 Tumor Proportion Score")))
              .setValue(percentageQuantity(sourceItem.getTpsScore())));
    }

    if (sourceItem.getCpsScore() != null) {
      target.addComponent(
          new Observation.ObservationComponentComponent()
              .setCode(
                  new CodeableConcept()
                      .addCoding(
                          new Coding()
                              .setSystem(
                                  "https://nih-ncpi.github.io/ncpi-fhir-ig/CodeSystem-ncit.html")
                              .setCode("C176582")
                              .setDisplay("PD-L1 Combined Positive Score")))
              .setValue(new Quantity().setValue(BigDecimal.valueOf(sourceItem.getCpsScore()))));
    }

    if (sourceItem.getIcScore() != null) {
      target.addComponent(
          new Observation.ObservationComponentComponent()
              .setCode(
                  new CodeableConcept()
                      .addCoding(
                          new Coding()
                              .setSystem(
                                  "https://nih-ncpi.github.io/ncpi-fhir-ig/CodeSystem-ncit.html")
                              .setCode("C199175")
                              .setDisplay("PD-L1 Immune Cell Score")))
              .setValue(thresholdPercentageQuantity(sourceItem.getIcScore().getCode().getValue())));
    }

    if (sourceItem.getTcScore() != null) {
      target.addComponent(
          new Observation.ObservationComponentComponent()
              .setCode(
                  new CodeableConcept()
                      .addCoding(
                          new Coding()
                              .setSystem("http://loinc.org")
                              .setCode("83053-9")
                              .setDisplay(
                                  "Cells.programmed cell death ligand 1/Viable tumor cells in Tissue by Immune stain")))
              .setValue(thresholdPercentageQuantity(sourceItem.getTcScore().getCode().getValue())));
    }
  }

  private Quantity percentageQuantity(Integer value) {
    return new Quantity()
        .setValue(BigDecimal.valueOf(value))
        .setUnit("%")
        .setSystem("http://unitsofmeasure.org")
        .setCode("%");
  }

  // For Ics-Score only codes 0-3 should be set. Tc-Score-codes go from 0 to 6
  private Quantity thresholdPercentageQuantity(String sourceCode) {
    switch (sourceCode) {
      case "0":
        return percentageQuantity(1).setComparator(Quantity.QuantityComparator.LESS_THAN);
      case "1":
        return percentageQuantity(1).setComparator(Quantity.QuantityComparator.GREATER_OR_EQUAL);
      case "2":
        return percentageQuantity(5).setComparator(Quantity.QuantityComparator.GREATER_OR_EQUAL);
      case "3":
        return percentageQuantity(10).setComparator(Quantity.QuantityComparator.GREATER_OR_EQUAL);
      case "4":
        return percentageQuantity(25).setComparator(Quantity.QuantityComparator.GREATER_OR_EQUAL);
      case "5":
        return percentageQuantity(50).setComparator(Quantity.QuantityComparator.GREATER_OR_EQUAL);
      case "6":
        return percentageQuantity(75).setComparator(Quantity.QuantityComparator.GREATER_OR_EQUAL);
      default:
        throw new IllegalArgumentException(
            String.format("Unsupported score code %s for PD-L1 IC/TC-Scores", sourceCode));
    }
  }

  @Override
  public List<Observation> mapToMany(IhcReport sourceItem) {
    return getProteinExpressions(sourceItem).stream().map(this::map).collect(Collectors.toList());
  }

  @Override
  public void addManyToBundle(Bundle bundle, IhcReport sourceItem) {
    getProteinExpressions(sourceItem).forEach(item -> this.addToBundle(bundle, item));
  }

  public List<Reference> getReferences(IhcReport sourceItem) {
    return getProteinExpressions(sourceItem).stream()
        .map(this::getReference)
        .collect(Collectors.toList());
  }

  private List<ProteinExpression> getProteinExpressions(IhcReport sourceItem) {
    if (sourceItem.getResults() == null || sourceItem.getResults().getProteinExpression() == null) {
      return List.of();
    }

    return sourceItem.getResults().getProteinExpression().stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private enum IhcProfile {
    GENERIC(
        "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-immunohistochemistry"),
    HER2(
        "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-immunohistochemistry-her2"),
    PDL1(
        "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-immunohistochemistry-pdl1");

    private final String canonicalUrl;

    IhcProfile(String canonicalUrl) {
      this.canonicalUrl = canonicalUrl;
    }

    private String getCanonicalUrl() {
      return canonicalUrl;
    }
  }
}
