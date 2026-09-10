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

import dev.pcvolkmer.mv64e.model.TumorSpecimen;
import dev.pcvolkmer.onco.datamapper.fhir.builders.ReferenceBuilder;
import dev.pcvolkmer.onco.datamapper.fhir.filter.Filter;
import java.util.List;
import org.hl7.fhir.r4.model.Specimen;
import org.hl7.fhir.r4.model.Type;
import org.jspecify.annotations.Nullable;

public class SpecimenMapper extends DnpmToFhirMapper<TumorSpecimen, Specimen> {

  public SpecimenMapper(ReferenceBuilder referenceBuilder, List<Filter<? extends Type>> filters) {
    super(referenceBuilder, filters);
  }

  @Override
  public String getPatientId(TumorSpecimen item) {
    return item.getPatient().getId();
  }

  @Override
  public String getId(TumorSpecimen item) {
    return item.getId();
  }

  @Override
  public String getFhirResourceType() {
    return "Specimen";
  }

  @Override
  public @Nullable Specimen map(TumorSpecimen sourceItem) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public String getSystem() {
    return String.format("%s/sid/specimen-id", this.fhirSystemBaseUrl);
  }
}
