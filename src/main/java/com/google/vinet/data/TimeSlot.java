/*
 *  Copyright 2020 Google LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https:www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.vinet.data;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class TimeSlot {

  protected final Instant start;
  protected final Instant end;
  protected final RegisteredUser registeredUser;
  private final Set<TimeSlot> neighbours = new HashSet<>();
  private TimeSlot pairedSlot = null;
  private double distance = 0;

  /**
   * Create a new time slot. Start and end times must not be null, and start time must come before
   * end time.
   *
   * @param start The starting instant for the slot.
   * @param end The ending instant for the slot.
   * @param registeredUser The user for which this slot applies.
   */
  public TimeSlot(Instant start, Instant end, RegisteredUser registeredUser) {
    this.start = Objects.requireNonNull(start, "Start time must not be null!");
    this.end = Objects.requireNonNull(end, "End time must not be null!");
    if (!start.isBefore(end)) throw new IllegalArgumentException();
    this.registeredUser = registeredUser;
  }

  public Instant getStart() {
    return start;
  }

  public Instant getEnd() {
    return end;
  }

  public double getDistance() {
    return distance;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }

  public boolean isPaired() {
    return pairedSlot != null;
  }

  public TimeSlot getPairedSlot() {
    return pairedSlot == null ? MatchingAlgorithm.NIL_NODE : pairedSlot;
  }

  public void setPairedSlot(TimeSlot pairedSlot) {
    this.pairedSlot = pairedSlot;
  }

  public Set<TimeSlot> getNeighbours() {
    return Collections.unmodifiableSet(neighbours);
  }

  public void addNeighbour(TimeSlot timeSlot) {
    neighbours.add(timeSlot);
  }

  /**
   * Checks whether this timeslot contains another
   *
   * @param timeSlot The slot that is to be contained
   * @return Whether this slot contains the specified slot
   */
  public boolean contains(TimeSlot timeSlot) {
    return (start.equals(timeSlot.start) || start.isBefore(timeSlot.start))
        && (end.equals(timeSlot.end) || end.isAfter(timeSlot.end));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TimeSlot timeSlot = (TimeSlot) o;
    return Objects.equals(start, timeSlot.start)
        && Objects.equals(end, timeSlot.end)
        && Objects.equals(registeredUser, timeSlot.registeredUser);
  }

  @Override
  public int hashCode() {
    return Objects.hash(start, end, registeredUser);
  }
}
