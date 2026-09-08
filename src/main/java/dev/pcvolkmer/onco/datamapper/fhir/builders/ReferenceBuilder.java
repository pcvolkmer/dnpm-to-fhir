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

package dev.pcvolkmer.onco.datamapper.fhir.builders;

import dev.pcvolkmer.onco.datamapper.fhir.DnpmToFhirMapper;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

public interface ReferenceBuilder {

  /**
   * Constructs a FHIR {@code Reference} object for the specified patient based on the provided
   * input item and a {@code DnpmToFhirMapper} implementation.
   *
   * @param <S> The source data type representing the input item.
   * @param <D> The FHIR resource type, extending {@code Resource}.
   * @param item The input object representing the patients data.
   * @param dnpmToFhirMapper An implementation of {@code DnpmToFhirMapper} containing the mapping
   *     logic and patient-specific details.
   * @return A {@code Reference} object pointing to the patient resource in the FHIR system.
   */
  <S, D extends Resource> Reference getPatientReference(
      S item, DnpmToFhirMapper<S, D> dnpmToFhirMapper);

  /**
   * Constructs a FHIR {@code Reference} object for the specified patient based on the provided
   * input item and a {@code DnpmToFhirMapper} implementation.
   *
   * @param <S> The source data item ID.
   * @param <D> The FHIR resource type, extending {@code Resource}.
   * @param id The input object id.
   * @param dnpmToFhirMapper An implementation of {@code DnpmToFhirMapper} containing the mapping
   *     logic and patient-specific details.
   * @return A {@code Reference} object pointing to the patient resource in the FHIR system.
   */
  <S, D extends Resource> Reference getPatientReference(
      String id, DnpmToFhirMapper<S, D> dnpmToFhirMapper);

  /**
   * Constructs a FHIR {@code Reference} object for the specified item based on the provided input
   * item and a {@code DnpmToFhirMapper} implementation.
   *
   * @param <S> The source data type representing the input item.
   * @param <D> The FHIR resource type, extending {@code Resource}.
   * @param item The input object representing the resource data.
   * @param dnpmToFhirMapper An implementation of {@code DnpmToFhirMapper} containing the mapping
   *     logic and patient-specific details.
   * @return A {@code Reference} object pointing to the resource in the FHIR system.
   */
  <S, D extends Resource> Reference getReference(S item, DnpmToFhirMapper<S, D> dnpmToFhirMapper);

  /**
   * Constructs a FHIR {@code Reference} object for the specified item based on the provided input
   * item and a {@code DnpmToFhirMapper} implementation.
   *
   * @param <S> The source data type representing the input item.
   * @param <D> The FHIR resource type, extending {@code Resource}.
   * @param id The input object id.
   * @param dnpmToFhirMapper An implementation of {@code DnpmToFhirMapper} containing the mapping
   *     logic and patient-specific details.
   * @return A {@code Reference} object pointing to the resource in the FHIR system.
   */
  <S, D extends Resource> Reference getReference(
      String id, DnpmToFhirMapper<S, D> dnpmToFhirMapper);
}
