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

package dev.pcvolkmer.onco.datamapper.fhir;

import static dev.pcvolkmer.onco.datamapper.fhir.DnpmToFhirTest.verify;

import dev.pcvolkmer.mv64e.model.Converter;
import dev.pcvolkmer.onco.datamapper.fhir.builders.DizUniMrReferenceBuilder;
import dev.pcvolkmer.onco.datamapper.fhir.builders.ToFhirReferenceBuilder;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class PatientRecordMapperTest {

  @Test
  void shouldMapExampleMtbFileUsingDizUniMrPatientReferences() throws IOException {
    var inputStream =
        Objects.requireNonNull(
            this.getClass().getClassLoader().getResourceAsStream("mv64e-mtb-fake-patient.json"));
    var mtb = Converter.fromJsonString(new String(inputStream.readAllBytes()));
    var fhir = PatientRecordMapper.customInstance(new DizUniMrReferenceBuilder()).mapToBundle(mtb);
    verify(fhir, "mv64e-mtb-fake-patient.json");
  }

  @Test
  void shouldMapExampleMtbFileUsingDefaultPatientReferences() throws IOException {
    var inputStream =
        Objects.requireNonNull(
            this.getClass().getClassLoader().getResourceAsStream("mv64e-mtb-fake-patient.json"));
    var mtb = Converter.fromJsonString(new String(inputStream.readAllBytes()));
    var fhir = PatientRecordMapper.defaultInstance().mapToBundle(mtb);
    verify(fhir, "mv64e-mtb-fake-patient.json");
  }

  @Test
  void shouldMapExampleMtbFileUsingToFhirReferences() throws IOException {
    var inputStream =
        Objects.requireNonNull(
            this.getClass().getClassLoader().getResourceAsStream("mv64e-mtb-fake-patient.json"));
    var mtb = Converter.fromJsonString(new String(inputStream.readAllBytes()));

    var baseUrl = "https://fhir.diz.uk-test.de";

    var properties =
        new FhirProperties(
            baseUrl,
            baseUrl + "/data-source/dnpm",
            new FhirProperties.Systems(
                Map.of(
                    "patient-id", baseUrl + "/identifiers/patient-id",
                    "specimen-id", baseUrl + "/identifiers/specimen-id",
                    "observation-id", baseUrl + "/identifiers/mtb-observation-id",
                    "condition-id", baseUrl + "/identifiers/mtb-condition-id",
                    "diagnostic-report-id", baseUrl + "/identifiers/mtb-diagnostic-report-id",
                    "careplan-id", baseUrl + "/identifiers/mtb-careplan-id",
                    "medication-request-id", baseUrl + "/identifiers/mtb-medication-request-id",
                    "service-request-id", baseUrl + "/identifiers/mtb-service-request-id")));

    var fhir =
        PatientRecordMapper.customInstance(new ToFhirReferenceBuilder(properties)).mapToBundle(mtb);

    verify(fhir, "mv64e-mtb-fake-patient.json");
  }
}
