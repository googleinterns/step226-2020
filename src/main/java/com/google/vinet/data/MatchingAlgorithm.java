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

import java.util.Set;

public class MatchingAlgorithm {

  /**
   * An implementation of the Hopcroft-Karp algorithm to match requested help times with volunteer
   * availability times.
   *
   * <p>The time slots must all belong to the same day. The algorithm also ensures a maximum
   * distance for volunteer action.
   *
   * @param isolateTimeSlots   The set of all requested time slots for help
   * @param volunteerTimeSlots The set of all time slots in which volunteers are available to help
   * @return A set of matched time slots, where a volunteer was matched to a requested time slot
   */
  public static Set<MatchedTimeSlot> matchTimeSlots(
          Set<IsolateTimeSlot> isolateTimeSlots, Set<VolunteerTimeSlot> volunteerTimeSlots) {
    // First step: add edges
    // For each volunteer time slot, check if it is contained by each isolate time slot.
    // if yes, add as a neighbour to both. This is an N^2 operation.
    for (VolunteerTimeSlot volunteerTimeSlot : volunteerTimeSlots) {
      for (IsolateTimeSlot isolateTimeSlot : isolateTimeSlots) {
        //TODO check if within geographic range
        if (isolateTimeSlot.contains(volunteerTimeSlot)) {
          isolateTimeSlot.addNeighbour(volunteerTimeSlot);
          volunteerTimeSlot.addNeighbour(isolateTimeSlot);
        }
      }
    }

    throw new UnsupportedOperationException();
  }
}
