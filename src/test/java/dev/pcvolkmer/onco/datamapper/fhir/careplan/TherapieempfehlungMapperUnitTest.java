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

import static org.assertj.core.api.Assertions.assertThat;

import dev.pcvolkmer.mv64e.model.AtcUnregisteredMedicationCoding;
import dev.pcvolkmer.mv64e.model.MtbMedicationRecommendation;
import dev.pcvolkmer.mv64e.model.MtbRecommendationPriorityCoding;
import dev.pcvolkmer.mv64e.model.Reference;
import dev.pcvolkmer.onco.datamapper.fhir.builders.DefaultReferenceBuilder;
import dev.pcvolkmer.onco.datamapper.fhir.filter.AtcCodeFilter;
import java.util.List;
import java.util.Set;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.junit.jupiter.api.Test;

class TherapieempfehlungMapperUnitTest {

  @Test
  void shouldFilterByAtcCode() {
    final var source =
        MtbMedicationRecommendation.builder()
            .patient(Reference.builder().id("42").build())
            .priority(
                MtbRecommendationPriorityCoding.builder()
                    .code(MtbRecommendationPriorityCoding.CodeEnum._1)
                    .build())
            .medication(
                Set.of(
                    AtcUnregisteredMedicationCoding.builder()
                        .code("A00BB12")
                        .system(
                            AtcUnregisteredMedicationCoding.SystemEnum
                                .HTTP_FHIR_DE_CODE_SYSTEM_BFARM_ATC)
                        .display("Medikament A")
                        .build(),
                    AtcUnregisteredMedicationCoding.builder()
                        .code("Unregistriertes Medikament B")
                        .system(AtcUnregisteredMedicationCoding.SystemEnum.UNDEFINED)
                        .display("Unregistriertes Medikament B")
                        .build()))
            .build();

    final var mapper =
        new TherapieempfehlungMapper(new DefaultReferenceBuilder(), List.of(new AtcCodeFilter()));

    final var actual = mapper.map(source);

    assertThat(actual).isNotNull();
    assertThat(actual.getMedication()).isInstanceOf(CodeableConcept.class);

    final var medication = (CodeableConcept) actual.getMedication();
    assertThat(medication.getCoding()).hasSize(1);
    assertThat(medication.getCoding().get(0).getSystem())
        .isEqualTo(
            AtcUnregisteredMedicationCoding.SystemEnum.HTTP_FHIR_DE_CODE_SYSTEM_BFARM_ATC
                .getValue());
  }
}
