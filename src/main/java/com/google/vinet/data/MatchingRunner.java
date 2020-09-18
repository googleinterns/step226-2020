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
  /** The Datastore to be used when fetching data and writing results. */
  private DatastoreService datastore;
  /** The TimeSlots for Isolate deliveries that should be matched. */
  private Set<IsolateTimeSlot> isolateTimeSlots;
  /** The TimeSlots for Volunteer availability that should be matched. */
  private Set<VolunteerTimeSlot> volunteerTimeSlots;
  /** The date whose matches will be computed. */
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
   * Any necessary data that is not already set will be pulled from DataStore, with an assumption
   * that matches should be created that will be scheduled for tomorrow.
   * @param deletePreviousMatches If set to true, all matches scheduled before today will be deleted;
   *                              today's matches will not be deleted.
   *                              If set to false, no deletions will be made.
   */
  public void run(boolean deletePreviousMatches) {
    if (datastore == null) datastore = DatastoreServiceFactory.getDatastoreService();
    if (date == null) date = LocalDate.now().plusDays(1);
    if (isolateTimeSlots == null) isolateTimeSlots = this.fetchIsolateTimeSlots();
    if (volunteerTimeSlots == null) volunteerTimeSlots = this.fetchVolunteerTimeSlots();

    if (deletePreviousMatches) {
      /* Delete all matches scheduled for dates before, but not including, today. */
      deletePreviousMatches(date, datastore);
    }

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

  /**
   * Delete all matches from the provided Datastore that are scheduled <em>before</em> the provided date.
   * Any matches scheduled on or after the date provided will not be deleted.
   * <b>IMPORTANT: The {@code date} parameter is EXCLUSIVE.</b>
   * @param cutoffDate The cutoff date for deciding if a Match will be deleted.
   * @param datastore The Datastore from which Matches should be deleted.
   */
  protected static void deletePreviousMatches(LocalDate cutoffDate, DatastoreService datastore){
    Query query = new Query("Matching")
                          .setFilter(new FilterPredicate("date", FilterOperator.LESS_THAN, cutoffDate.toString()))
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
  protected Set<IsolateTimeSlot> fetchIsolateTimeSlots() {
    return fetchIsolateTimeSlots(this.date, this.datastore);
  }

  /**
   * @return the VolunteerTimeSlots scheduled for this MatchingRunner's date from this MatchingRunner's implementation of DataStore.
   */
  protected Set<VolunteerTimeSlot> fetchVolunteerTimeSlots() {
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
  protected static PreparedQuery getTimeSlotsQuery(
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
  protected static Set<IsolateTimeSlot> fetchIsolateTimeSlots(LocalDate date, DatastoreService datastore) {
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
  protected static Set<VolunteerTimeSlot> fetchVolunteerTimeSlots(LocalDate date, DatastoreService datastore) {
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
