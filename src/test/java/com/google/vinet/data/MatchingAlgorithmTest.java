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

  @BeforeEach
  public void initialiseSets() {
    isolateTimeSlots = new HashSet<>();
    volunteerTimeSlots = new HashSet<>();
  }

  @Test
  public void testOneRequestOneVolunteerMatch() {
    Instant now = Instant.now();
    IsolateTimeSlot isolateSlot = new IsolateTimeSlot(now, now.plus(1, HOURS), null);
    VolunteerTimeSlot volunteerSlot = new VolunteerTimeSlot(now, now.plus(1, HOURS), null);
    isolateTimeSlots.add(isolateSlot);
    volunteerTimeSlots.add(volunteerSlot);
    Set<IsolateTimeSlot> matched = MatchingAlgorithm.matchTimeSlots(isolateTimeSlots, volunteerTimeSlots);
    assert (matched.contains(isolateSlot));
    assert (isolateSlot.isPaired());
    assert (isolateSlot.getPairedSlot().equals(volunteerSlot));
  }
}
