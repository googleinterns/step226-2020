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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static java.time.temporal.ChronoUnit.HOURS;

public class MatchingAlgorithmTest {

  private Set<IsolateTimeSlot> isolateTimeSlots;
  private Set<VolunteerTimeSlot> volunteerTimeSlots;
  private Instant now;

  @BeforeEach
  public void initialiseSets() {
    isolateTimeSlots = new HashSet<>();
    volunteerTimeSlots = new HashSet<>();
    now = Instant.now();
  }

  @Test
  public void testOneRequestOneVolunteerMatch() {
    IsolateTimeSlot isolateSlot = new IsolateTimeSlot(now, now.plus(1, HOURS), null);
    VolunteerTimeSlot volunteerSlot = new VolunteerTimeSlot(now, now.plus(1, HOURS), null);
    isolateTimeSlots.add(isolateSlot);
    volunteerTimeSlots.add(volunteerSlot);
    Set<IsolateTimeSlot> matched = MatchingAlgorithm.matchTimeSlots(isolateTimeSlots, volunteerTimeSlots);
    assert (matched.contains(isolateSlot));
    assert (isolateSlot.isPaired());
    assert (isolateSlot.getPairedSlot() == volunteerSlot);
  }


  @Test
  public void testOneRequestOneVolunteerNoMatch() {
    IsolateTimeSlot isolateSlot = new IsolateTimeSlot(now.plus(1, HOURS), now.plus(2, HOURS), null);
    VolunteerTimeSlot volunteerSlot = new VolunteerTimeSlot(now, now.plus(1, HOURS), null);
    isolateTimeSlots.add(isolateSlot);
    volunteerTimeSlots.add(volunteerSlot);
    Set<IsolateTimeSlot> matched = MatchingAlgorithm.matchTimeSlots(isolateTimeSlots, volunteerTimeSlots);
    assert (!matched.contains(isolateSlot));
    assert (!isolateSlot.isPaired());
    assert (isolateSlot.getPairedSlot() != volunteerSlot);
  }

  @Test
  public void testOneRequestOneVolunteerVolunteerTooLongMatch() {
    IsolateTimeSlot isolateSlot = new IsolateTimeSlot(now, now.plus(1, HOURS), null);
    VolunteerTimeSlot volunteerSlot = new VolunteerTimeSlot(now, now.plus(2, HOURS), null);
    isolateTimeSlots.add(isolateSlot);
    volunteerTimeSlots.add(volunteerSlot);
    Set<IsolateTimeSlot> matched = MatchingAlgorithm.matchTimeSlots(isolateTimeSlots, volunteerTimeSlots);
    assert (matched.contains(isolateSlot));
    assert (isolateSlot.isPaired());
    assert (isolateSlot.getPairedSlot() == volunteerSlot);
  }

  /**
   * This tests having two requested hours that both fit within the volunteer's available time slot.
   * Because we are doing bipartite matching, this means only one of the requests will be matched.
   * Either of the isolate timeslots could be assigned.
   */
  @Test
  public void testTwoRequestsOneVolunteerVolunteerTooLongMatch() {
    IsolateTimeSlot isolateSlot1 = new IsolateTimeSlot(now, now.plus(1, HOURS), null);
    IsolateTimeSlot isolateSlot2 = new IsolateTimeSlot(now.plus(1, HOURS), now.plus(2, HOURS), null);
    VolunteerTimeSlot volunteerSlot = new VolunteerTimeSlot(now, now.plus(2, HOURS), null);
    isolateTimeSlots.add(isolateSlot1);
    isolateTimeSlots.add(isolateSlot2);
    volunteerTimeSlots.add(volunteerSlot);
    Set<IsolateTimeSlot> matched = MatchingAlgorithm.matchTimeSlots(isolateTimeSlots, volunteerTimeSlots);
    assert (matched.size() == 1);
  }

  @Test
  public void testTwoRequestsTwoVolunteersVolunteerTooLongMatch() {
    IsolateTimeSlot isolateSlot1 = new IsolateTimeSlot(now, now.plus(1, HOURS), null);
    IsolateTimeSlot isolateSlot2 = new IsolateTimeSlot(now.plus(1, HOURS), now.plus(2, HOURS), null);
    VolunteerTimeSlot volunteerSlot1 = new VolunteerTimeSlot(now, now.plus(2, HOURS), null);
    VolunteerTimeSlot volunteerSlot2 = new VolunteerTimeSlot(now, now.plus(1, HOURS), null);
    isolateTimeSlots.add(isolateSlot1);
    isolateTimeSlots.add(isolateSlot2);
    volunteerTimeSlots.add(volunteerSlot1);
    volunteerTimeSlots.add(volunteerSlot2);
    Set<IsolateTimeSlot> matched = MatchingAlgorithm.matchTimeSlots(isolateTimeSlots, volunteerTimeSlots);
    assert (matched.size() == 2);
    assert (isolateSlot1.getPairedSlot() != (isolateSlot2.getPairedSlot()));
  }


  //TODO test with no volunteers
  //TODO test with no isolates
  //TODO test with neither
  //TODO test with null sets
}
