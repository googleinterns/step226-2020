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
import java.util.*;

public class MatchingRunner {
  private DatastoreService datastore;
  private Set<IsolateTimeSlot> isolateTimeSlots;
  private Set<VolunteerTimeSlot> volunteerTimeSlots;
  private LocalDate date;

  /**
   * Construct a MatchingRunner with none of its data set.
   */
  public MatchingRunner() {

  }

  /**
   * Construct a matching runner with a dependency on the provided DataStore implementation.
   * @param datastore The DataStore implementation that this MatchingRunnner will depend on.
   */
  protected MatchingRunner(DatastoreService datastore) {
    Objects.requireNonNull(datastore);
    this.datastore = datastore;
  }

  /**
   * Run the matching algorithm and store the results in DataStore.
   * Any necessary data that is not already set will be pulled from DataStore.
   */
  public void run() {
    if (this.datastore == null) this.datastore = DatastoreServiceFactory.getDatastoreService();
    if (this.date == null) this.date = LocalDate.now().plusDays(1);
    if (this.isolateTimeSlots == null) this.isolateTimeSlots = this.fetchIsolateTimeSlots();
    if (this.volunteerTimeSlots == null) this.volunteerTimeSlots = this.fetchVolunteerTimeSlots();

    deletePreviousMatches(this.date, this.datastore);

    final Set<IsolateTimeSlot> matches = MatchingAlgorithm.matchTimeSlots(isolateTimeSlots, volunteerTimeSlots);

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

  public static void deletePreviousMatches(LocalDate date, DatastoreService datastore){
    Query query = new Query("Matching")
                          .setFilter(new FilterPredicate("date", FilterOperator.LESS_THAN, date.toString()))
                              .setKeysOnly();

    PreparedQuery preparedQuery = datastore.prepare(query);

    List<Entity> oldEntries =  preparedQuery.asList(FetchOptions.Builder.withDefaults());

    for (Entity entry : oldEntries){
      datastore.delete(entry.getKey());
    }
  }

  /**
   * @return the IsolateTimeSlots scheduled for this MatchingRunner's date from this MatchingRunner's implementation of DataStore.
   */
  public Set<IsolateTimeSlot> fetchIsolateTimeSlots() {
    return fetchIsolateTimeSlots(this.date, this.datastore);
  }

  /**
   * @return the VolunteerTimeSlots scheduled for this MatchingRunner's date from this MatchingRunner's implementation of DataStore.
   */
  public Set<VolunteerTimeSlot> fetchVolunteerTimeSlots() {
    return fetchVolunteerTimeSlots(this.date, this.datastore);
  }

  /**
   * Get a PreparedQuery which will return all TimeSlots with the date and entity type provided when
   * executed using the provided DataStore implementation.
   *
   * @param entityName The entity name of the TimeSlots to be fetched.
   * @param date The date that the fetched TimeSlots must be scheduled on.
   * @param datastore The DataStore implementation to be used when preparing the query.
   * @return A PreparedQuery which will return all TimeSlots with the date and entity type provided
   * when executed using the provided DataStore implementation.
   */
  public static PreparedQuery getTimeSlotsQuery(
      String entityName, LocalDate date, DatastoreService datastore) {
    final Query query = new Query(entityName);

    final Filter dateFilter = new FilterPredicate("date", FilterOperator.EQUAL, date.toString());

    query.setFilter(dateFilter);

    return datastore.prepare(query);
  }

  /**
   * Fetch all IsolateTimeSlots scheduled for the provided date using the provided DataStore implementation.
   * @param date The date to filter the IsolateTimeSlots by.
   * @param datastore The DataStore implementation to be queried.
   * @return All IsolateTimeSlots scheduled for the provided date using the provided DataStore implementation.
   */
  public static Set<IsolateTimeSlot> fetchIsolateTimeSlots(LocalDate date, DatastoreService datastore) {
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

  /**
   * Fetch all VolunteerTimeSlots scheduled for the provided date using the provided DataStore implementation.
   * @param date The date to filter the VolunteerTimeSlots by.
   * @param datastore The DataStore implementation to be queried.
   * @return All VolunteerTimeSlots scheduled for the provided date using the provided DataStore implementation.
   */
  public static Set<VolunteerTimeSlot> fetchVolunteerTimeSlots(LocalDate date, DatastoreService datastore) {
    final PreparedQuery preparedQuery = getTimeSlotsQuery("volunteer_timeslots", date, datastore);

    final FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();

    final Set<VolunteerTimeSlot> timeSlots = new HashSet<>();

    for (Entity entity : preparedQuery.asIterable(fetchOptions)) {
      final Volunteer volunteer = new Volunteer((String) entity.getProperty("userId"));
      final Instant start = Instant.parse((String) entity.getProperty("start"));
      final Instant end = Instant.parse((String) entity.getProperty("end"));
      final VolunteerTimeSlot volunteerTimeSlot = new VolunteerTimeSlot(start, end, volunteer);
      timeSlots.add(volunteerTimeSlot);
    }

    return timeSlots;
  }

  /**
   * Set the IsolateTimeSlots to be used for matching by this MatchingRunner.
   * @param isolateTimeSlots The IsolateTimeSlots to be matched.
   * @throws NullPointerException If isolateTimeSlots is null.
   */
  public void setIsolateTimeSlots(Set<IsolateTimeSlot> isolateTimeSlots) {
    Objects.requireNonNull(isolateTimeSlots);
    this.isolateTimeSlots = isolateTimeSlots;
  }

  /**
   * Set the VolunteerTimeSlots to be used for matching by this MatchingRunner.
   * @param volunteerTimeSlots The volunteerTimeSlots to be matched.
   * @throws NullPointerException If volunteerTimeSlots is null.
   */
  public void setVolunteerTimeSlots(Set<VolunteerTimeSlot> volunteerTimeSlots) {
    Objects.requireNonNull(volunteerTimeSlots);
    this.volunteerTimeSlots = volunteerTimeSlots;
  }
}
