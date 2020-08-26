/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.vinet.data;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public abstract class TimeSlot implements Comparable<TimeSlot> {

  private final Instant start;
  private final Instant end;
  private final Set<TimeSlot> neighbours = new HashSet<>();

  protected TimeSlot(Instant start, Instant end) {
    this.start = start;
    this.end = end;
  }

  public Instant getStart() {
    return start;
  }

  public Instant getEnd() {
    return end;
  }

  public Set<TimeSlot> getNeighbours() {
    return Collections.unmodifiableSet(neighbours);
  }

  public void addNeighbour(TimeSlot timeSlot) {
    neighbours.add(timeSlot);
  }

  public void removeNeighbour(TimeSlot timeSlot) {
    neighbours.remove(timeSlot);
  }

  /**
   * Compares two time slots based on their start time
   */
  @Override
  public int compareTo(TimeSlot timeSlot) {
    return start.compareTo(timeSlot.getStart());
  }

  public static Comparator<TimeSlot> TimeSlotEndComparator = Comparator.comparing(TimeSlot::getEnd);

  /**
   * Checks whether this timeslot contains another
   *
   * @param timeSlot The slot that is to be contained
   * @return Whether this slot contains the specified slot
   */
  public boolean contains(TimeSlot timeSlot) {
    return compareTo(timeSlot) >= 0 && TimeSlotEndComparator.compare(this, timeSlot) <= 0;
  }
}
