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
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class MatchingAlgorithm {

  /**
   * Represents a node connected to all isolate time slot nodes
   */
  public static final IsolateTimeSlot NIL_NODE =
          new IsolateTimeSlot(Instant.MIN, Instant.MIN, null);

  /**
   * An implementation of the Hopcroft-Karp algorithm to match requested help times with volunteer
   * availability times.
   *
   * <p>This method has a worst-case run time of O(N*M + E*sqrt(N+M)), where N is the number of
   * volunteer time slots, M the number of isolate time slots, and E the number of edges between the
   * two sets. The O(N*M) is due to the edge creation before running the algorithm.
   *
   * @param isolateTimeSlots   The set of all requested time slots for help
   * @param volunteerTimeSlots The set of all time slots in which volunteers are available to help
   * @return A set of matched time slots, where a volunteer was matched to a requested time slot
   */
  public static Set<IsolateTimeSlot> matchTimeSlots(
          Set<IsolateTimeSlot> isolateTimeSlots, Set<VolunteerTimeSlot> volunteerTimeSlots) {

    if (isolateTimeSlots == null || volunteerTimeSlots == null)
      throw new IllegalArgumentException("Null argument!");

    // Remove null nodes, if present
    isolateTimeSlots.remove(null);
    volunteerTimeSlots.remove(null);

    addEdges(isolateTimeSlots, volunteerTimeSlots);
    isolateTimeSlots.add(NIL_NODE);

    while (breadthFirstSearch(isolateTimeSlots)) {
      for (IsolateTimeSlot isolateTimeSlot : isolateTimeSlots) {
        if (!isolateTimeSlot.isPaired()) {
          depthFirstSearch(isolateTimeSlot);
        }
      }
    }

    return isolateTimeSlots.stream().filter(TimeSlot::isPaired).collect(Collectors.toSet());
  }

  /**
   * Adds edges between the two sets of time slots, based on availability constraints.
   *
   * <p>Adds an edge if a volunteer time slot contains an isolate time slot.
   *
   * <p>This is an N*M operation, where N is the number of isolate time slots and M the number of
   * volunteer time slots.
   */
  private static void addEdges(
          Set<IsolateTimeSlot> isolateTimeSlots, Set<VolunteerTimeSlot> volunteerTimeSlots) {
    for (VolunteerTimeSlot volunteerTimeSlot : volunteerTimeSlots) {
      for (IsolateTimeSlot isolateTimeSlot : isolateTimeSlots) {
        // TODO check if within geographic range
        // TODO consider the case where volunteer time slot is longer than isolate's
        // we could run the algorithm again with the remaining isolate slots and the chunks of
        // volunteer time slots that were not assigned

        // Remove time slot if any of its instants are null
        if (volunteerTimeSlot.getStart() == null || volunteerTimeSlot.getEnd() == null) {
          volunteerTimeSlots.remove(volunteerTimeSlot);
          continue;
        } else if (isolateTimeSlot.getStart() == null || isolateTimeSlot.getEnd() == null) {
          isolateTimeSlots.remove(isolateTimeSlot);
          continue;
        }

        if (volunteerTimeSlot.contains(isolateTimeSlot)) {
          isolateTimeSlot.addNeighbour(volunteerTimeSlot);
          volunteerTimeSlot.addNeighbour(isolateTimeSlot);
        }
      }
    }
  }

  /**
   * Performs breadth-first search (BFS) from one set of time slots to the other.
   *
   * @param isolateTimeSlots The set of isolate-requested time slots.
   * @return True if a path was found between two unmatched time slots.
   */
  private static boolean breadthFirstSearch(Set<IsolateTimeSlot> isolateTimeSlots) {
    Queue<TimeSlot> queue = new LinkedList<>();
    for (IsolateTimeSlot isolateTimeSlot : isolateTimeSlots) {
      if (!isolateTimeSlot.isPaired()) {
        isolateTimeSlot.setDistance(0);
        queue.add(isolateTimeSlot);
      } else {
        isolateTimeSlot.setDistance(Double.POSITIVE_INFINITY);
      }
    }

    NIL_NODE.setDistance(Double.POSITIVE_INFINITY);

    while (!queue.isEmpty()) {
      TimeSlot slot = queue.remove();
      final double slotDistance = slot.getDistance();
      if (slotDistance < NIL_NODE.getDistance()) {
        for (TimeSlot neighbour : slot.getNeighbours()) {
          TimeSlot neighbourPair = neighbour.getPairedSlot();
          if (neighbourPair.getDistance() == Double.POSITIVE_INFINITY) {
            neighbourPair.setDistance(slotDistance + 1);
            queue.add(neighbourPair);
          }
        }
      }
    }

    return NIL_NODE.getDistance() != Double.POSITIVE_INFINITY;
  }

  /**
   * Performs depth-first search from the given time slot to the first unmatched time slot it finds
   *
   * @param slot The slot to use as a starting point for the search
   * @return Whether the search found an unmatched slot
   */
  private static boolean depthFirstSearch(TimeSlot slot) {
    if (slot.equals(NIL_NODE)) return true;
    for (TimeSlot neighbour : slot.getNeighbours()) {
      TimeSlot neighbourPair = neighbour.getPairedSlot();
      if (neighbourPair.getDistance() == slot.getDistance() + 1) {
        if (depthFirstSearch(neighbourPair)) {
          neighbour.setPairedSlot(slot);
          slot.setPairedSlot(neighbour);
          return true;
        }
      }
    }
    slot.setDistance(Double.POSITIVE_INFINITY);
    return false;
  }
}
