/*
 * Copyright 2022 Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.confluent.kafka.schemaregistry.client.rest.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Metadata, which includes field annotations, arbitrary key-value properties,
 * and a list of reference names for included properties.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metadata {

  public static final Metadata EMPTY_METADATA =
      new Metadata(
          Collections.emptySortedMap(), Collections.emptySortedMap(), Collections.emptySortedSet());

  @JsonPropertyOrder(alphabetic = true)
  private final SortedMap<String, SortedSet<String>> annotations;
  @JsonPropertyOrder(alphabetic = true)
  private final SortedMap<String, String> properties;
  private final SortedSet<String> sensitive;

  @JsonCreator
  public Metadata(
      @JsonProperty("annotations") Map<String, ? extends Set<String>> annotations,
      @JsonProperty("properties") Map<String, String> properties,
      @JsonProperty("sensitive") Set<String> sensitive
  ) {
    SortedMap<String, SortedSet<String>> sortedAnnotations = annotations != null
        ? annotations.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .collect(Collectors.toMap(
            Entry::getKey,
            e -> e.getValue().stream().sorted().collect(Collectors.toCollection(TreeSet::new)),
            (e1, e2) -> e1,
            TreeMap::new))
        : Collections.emptySortedMap();
    SortedMap<String, String> sortedProperties = properties != null
        ? properties.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .collect(Collectors.toMap(
            Entry::getKey,
            Entry::getValue,
            (e1, e2) -> e1,
            TreeMap::new))
        : Collections.emptySortedMap();
    SortedSet<String> sortedSensitive = sensitive != null
        ? sensitive.stream()
        .sorted()
        .collect(Collectors.toCollection(TreeSet::new))
        : Collections.emptySortedSet();
    this.annotations = Collections.unmodifiableSortedMap(sortedAnnotations);
    this.properties = Collections.unmodifiableSortedMap(sortedProperties);
    this.sensitive = Collections.unmodifiableSortedSet(sortedSensitive);
  }

  public SortedMap<String, SortedSet<String>> getAnnotations() {
    return annotations;
  }

  public SortedMap<String, String> getProperties() {
    return properties;
  }

  public SortedSet<String> getSensitive() {
    return sensitive;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Metadata metadata = (Metadata) o;
    return Objects.equals(annotations, metadata.annotations)
        && Objects.equals(properties, metadata.properties)
        && Objects.equals(sensitive, metadata.sensitive);
  }

  @Override
  public int hashCode() {
    return Objects.hash(annotations, properties, sensitive);
  }

  @Override
  public String toString() {
    return "Metadata{"
        + "annotations=" + annotations
        + ", properties=" + properties
        + ", sensitive=" + sensitive
        + '}';
  }

  public void updateHash(MessageDigest md) {
    if (annotations != null) {
      annotations.forEach((key, value) -> {
        md.update(key.getBytes(StandardCharsets.UTF_8));
        value.forEach(v -> md.update(v.getBytes(StandardCharsets.UTF_8)));
      });
    }
    if (properties != null) {
      properties.forEach((key, value) -> {
        md.update(key.getBytes(StandardCharsets.UTF_8));
        md.update(value.getBytes(StandardCharsets.UTF_8));
      });
    }
    if (sensitive != null) {
      sensitive.forEach(s -> md.update(s.getBytes(StandardCharsets.UTF_8)));
    }
  }
}
