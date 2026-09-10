package dev.pcvolkmer.onco.datamapper.fhir.filter;

import java.util.List;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.Type;

public interface Filter<T extends Type> {

  boolean matchesRequirement(T item);

  default List<T> filter(List<T> items) {
    return items.stream().filter(this::matchesRequirement).collect(Collectors.toList());
  }
}
