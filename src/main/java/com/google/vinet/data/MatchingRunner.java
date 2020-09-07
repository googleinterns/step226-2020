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

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;

public class MatchingRunner {
  private DatastoreService datastore;
  private Set<IsolateTimeSlot> isolateTimeSlots;
  private Set<VolunteerTimeSlot> volunteerTimeSlots;
  private LocalDate date;

  public MatchingRunner() {

  }

  protected MatchingRunner(DatastoreService datastore) {
    assert(datastore != null);
    this.datastore = datastore;
  }

  public void run() {
    if (this.datastore == null) this.datastore = DatastoreServiceFactory.getDatastoreService();
    if (this.date == null) this.date = LocalDate.now().plusDays(1);
    if (this.isolateTimeSlots == null) this.isolateTimeSlots = this.getIsolateTimeSlots();
    if (this.volunteerTimeSlots == null) this.volunteerTimeSlots = this.getVolunteerTimeSlots();

    System.out.println("========ISOLATES========");
    System.out.println(this.isolateTimeSlots);
    System.out.println("========ISOLATES========");
    System.out.println("========VOLUNTEERS========");
    System.out.println(this.volunteerTimeSlots);
    System.out.println("========VOLUNTEERS========");

    final Set<IsolateTimeSlot> matches = MatchingAlgorithm.matchTimeSlots(isolateTimeSlots, volunteerTimeSlots);

    System.out.println("========MATCHES========");
    System.out.println(matches);
    System.out.println("========MATCHES========");

    for (IsolateTimeSlot matching : matches) {
      Entity matchingEntity = new Entity("Matching");
      matchingEntity.setProperty("isolateId", matching.getIsolate().userId);
      matchingEntity.setProperty("volunteerId", matching.getPairedSlot().registeredUser.userId);
      matchingEntity.setProperty("date", matching.date.toString());
      matchingEntity.setProperty("start", matching.start.toString());
      matchingEntity.setProperty("end", matching.end.toString());
      matchingEntity.setProperty("ticket", KeyFactory.keyToString(matching.ticket));
      datastore.put(matchingEntity);
    }
  }

  public Set<IsolateTimeSlot> getIsolateTimeSlots() {
    return getIsolateTimeSlots(this.date, this.datastore);
  }

  public Set<VolunteerTimeSlot> getVolunteerTimeSlots() {
    return getVolunteerTimeSlots(this.date, this.datastore);
  }

  public static PreparedQuery getTimeSlotsQuery(String entityName, LocalDate date, DatastoreService datastore) {
    final Query query = new Query(entityName);

    final Filter dateFilter = new FilterPredicate("date", FilterOperator.EQUAL, date.toString());

    query.setFilter(dateFilter);

    final PreparedQuery preparedQuery = datastore.prepare(query);

    return preparedQuery;
  }

  public static Set<IsolateTimeSlot> getIsolateTimeSlots(LocalDate date, DatastoreService datastore) {
    PreparedQuery preparedQuery =  getTimeSlotsQuery("IsolateTimeSlot", date, datastore);

    final FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();

    final Set<IsolateTimeSlot> timeSlots = new HashSet<>();

    for (Entity entity : preparedQuery.asIterable(fetchOptions)) {
      final Isolate isolate = new Isolate((String) entity.getProperty("isolateId"));
      final Key ticketKey = KeyFactory.stringToKey((String) entity.getProperty("ticketKey"));
      final LocalDate localDate = LocalDate.parse((String) entity.getProperty("date"));
      final Instant start = Instant.parse((String) entity.getProperty("startTime"));
      final Instant end = Instant.parse((String) entity.getProperty("endTime"));
      final IsolateTimeSlot isolateTimeSlot = new IsolateTimeSlot(start, end, isolate, localDate, ticketKey);
      timeSlots.add(isolateTimeSlot);
    }

    return timeSlots;
  }

  public static Set<VolunteerTimeSlot> getVolunteerTimeSlots(LocalDate date, DatastoreService datastore) {
    final PreparedQuery preparedQuery = getTimeSlotsQuery("volunteer_timeslots", date, datastore);

    final FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();

    final Set<VolunteerTimeSlot> timeSlots = new HashSet<>();

    for (Entity entity : preparedQuery.asIterable(fetchOptions)) {
      final Volunteer volunteer = new Volunteer((String) entity.getProperty("userId"));
      final Instant start = Instant.parse((String) entity.getProperty("startTime"));
      final Instant end = Instant.parse((String) entity.getProperty("endTime"));
      final VolunteerTimeSlot volunteerTimeSlot = new VolunteerTimeSlot(start, end, volunteer);
      timeSlots.add(volunteerTimeSlot);
    }

    return timeSlots;
  }

  public void setIsolateTimeSlots(Set<IsolateTimeSlot> isolateTimeSlots) {
    assert(isolateTimeSlots != null);
    this.isolateTimeSlots = isolateTimeSlots;
  }

  public void setVolunteerTimeSlots(Set<VolunteerTimeSlot> volunteerTimeSlots) {
    assert(volunteerTimeSlots != null);
    this.volunteerTimeSlots = volunteerTimeSlots;
  }
}
