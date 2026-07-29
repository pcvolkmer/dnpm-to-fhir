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

package dev.pcvolkmer.onco.datamapper.fhir.ngs;

import dev.pcvolkmer.mv64e.model.DnaFusion;
import org.hl7.fhir.r4.model.*;

public class DnaFusionMapper extends AbstractNgsMapper<DnaFusion> {
  @Override
  protected String getPatientId(DnaFusion item) {
    return item.getPatient().getId();
  }

  @Override
  protected String getId(DnaFusion item) {
    return String.format("%s_ngsdnafusion", item.getId());
  }

  @Override
  public Observation map(DnaFusion sourceItem) {
    var result = new Observation();
    result.addIdentifier().setSystem(this.getSystem()).setValue(this.getId(sourceItem));

    result.setMeta(
        new Meta()
            .setSource(this.fhirMetaSource)
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-dna-fusion"));

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
        .setSystem("http://terminology.hl7.org/CodeSystem/v2-0074")
        .setCode("GE");

    result.setCode(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setCode("69548-6")
                    .setSystem("http://loinc.org")
                    .setDisplay("Genetic variant assessment")));

    result.setMethod(
        new CodeableConcept()
            .addCoding(new Coding().setCode("LA4048-6").setSystem("http://loinc.org")));

    // Number reported reads
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(new Coding().setCode("82121-5").setSystem("http://loinc.org")))
            .setValue(new Quantity(sourceItem.getReportedNumReads())));

    // 5' Fusion Partner
    final var fivePrime = sourceItem.getFusionPartner5prime();
    if (null == fivePrime) {
      throw new IllegalArgumentException("No 5' fusion partner given!");
    }

    // Chromosome
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode("five-prime-chromosome")
                            .setSystem(
                                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/CodeSystem/mii-cs-mtb-molekulare-biomarker")
                            .setDisplay("Five Prime Chromosome")))
            .setValue(
                new CodeableConcept().addCoding(this.mapChromosome(fivePrime.getChromosome()))));

    // Gene
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode("five-prime-gene")
                            .setSystem(
                                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/CodeSystem/mii-cs-mtb-molekulare-biomarker")
                            .setDisplay("Five Prime Gene")))
            .setValue(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode(fivePrime.getGene().getCode())
                            .setSystem("https://www.genenames.org/")
                            .setDisplay(fivePrime.getGene().getDisplay()))));

    // Position
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode("five-prime-position")
                            .setSystem(
                                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/CodeSystem/mii-cs-mtb-molekulare-biomarker")
                            .setDisplay("Five Prime Position")))
            .setValue(new Quantity().setValue(fivePrime.getPosition())));

    // 3' Fusion Partner
    final var threePrime = sourceItem.getFusionPartner3prime();
    if (null == threePrime) {
      throw new IllegalArgumentException("No 3' fusion partner given!");
    }

    // Chromosome
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode("three-prime-chromosome")
                            .setSystem(
                                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/CodeSystem/mii-cs-mtb-molekulare-biomarker")
                            .setDisplay("Three Prime Chromosome")))
            .setValue(
                new CodeableConcept().addCoding(this.mapChromosome(threePrime.getChromosome()))));

    // Gene
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode("three-prime-gene")
                            .setSystem(
                                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/CodeSystem/mii-cs-mtb-molekulare-biomarker")
                            .setDisplay("Three Prime Gene")))
            .setValue(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode(threePrime.getGene().getCode())
                            .setSystem("https://www.genenames.org/")
                            .setDisplay(threePrime.getGene().getDisplay()))));

    // Position
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode("three-prime-position")
                            .setSystem(
                                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/CodeSystem/mii-cs-mtb-molekulare-biomarker")
                            .setDisplay("Three Prime Position")))
            .setValue(new Quantity().setValue(threePrime.getPosition())));

    result.setSubject(this.getPatientReference(sourceItem));

    return result;
  }
}
