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

import dev.pcvolkmer.onco.datamapper.fhir.builders.ReferenceBuilder;
import dev.pcvolkmer.onco.datamapper.fhir.filter.Filter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Type;

public abstract class DnpmToFhirMapper<S, D extends Resource> implements Mapper<S, D> {

  private final MessageDigest md5Digest;

  protected String fhirSystemBaseUrl = "https://fhir.diz.uni-marburg.de";

  protected String fhirMetaSource = String.format("%s/data-source/dnpm", this.fhirSystemBaseUrl);

  protected ReferenceBuilder referenceBuilder;
  protected List<Filter<? extends Type>> filters;

  protected DnpmToFhirMapper(
      ReferenceBuilder referenceBuilder, List<Filter<? extends Type>> filters) {
    this.referenceBuilder = referenceBuilder;
    this.filters = filters;
    try {
      md5Digest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public abstract String getPatientId(S item);

  protected Reference getPatientReference(S item) {
    return this.referenceBuilder.getPatientReference(item, this);
  }

  public Reference getReference(S item) {
    return new Reference().setReference(this.getRequestUrl(item));
  }

  @Override
  public void addToBundle(Bundle bundle, S item) {
    final var resource = map(item);
    if (null != resource) {
      bundle
          .addEntry()
          .setResource(resource)
          .setFullUrl(this.fullUrlUrn(getRequestUrl(item)))
          .getRequest()
          .setMethod(Bundle.HTTPVerb.PUT)
          .setUrl(getRequestUrl(item));
    }
  }

  public String fullUrlUrn(String requestUrl) {
    final var digest = this.md5Digest.digest(requestUrl.getBytes(StandardCharsets.UTF_8));
    ByteBuffer bb = ByteBuffer.wrap(digest);
    long mostSigBits = bb.getLong();
    long leastSigBits = bb.getLong();

    final var uuid = new UUID(mostSigBits, leastSigBits);
    return String.format("urn:uuid:%s", uuid);
  }

  public abstract String getId(S item);

  public abstract String getFhirResourceType();

  protected String getRequestUrl(S item) {
    return this.referenceBuilder.getReference(item, this).getReference();
  }
}
