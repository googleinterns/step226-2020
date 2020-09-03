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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class VolunteerTimeSlot extends TimeSlot implements Datastoreable {
  private static DatastoreService datastoreService;
  public static final String VOLUNTEER_TIMESLOT_TABLE_NAME = "volunteer_timeslots";

  public VolunteerTimeSlot(Instant start, Instant end, Volunteer volunteer) {
    super(start, end, volunteer);
  }

  public VolunteerTimeSlot(Entity entity) {
    this(
            Instant.parse((String) entity.getProperty("start")),
            Instant.parse((String) entity.getProperty("end")),
            new Volunteer((String) entity.getProperty("userId")));
  }

  public Volunteer getVolunteer() {
    return (Volunteer) registeredUser;
  }

  @Override
  public void toDatastore() {
    if (datastoreService == null) datastoreService = DatastoreServiceFactory.getDatastoreService();

    final Entity entity = new Entity(VOLUNTEER_TIMESLOT_TABLE_NAME);
    entity.setProperty("userId", registeredUser.getUserId());
    entity.setProperty("start", getStart().toString());
    entity.setProperty("end", getEnd().toString());

    datastoreService.put(entity);
  }

  public static List<VolunteerTimeSlot> getTimeslotsByUserId(String userId) {
    return StreamSupport.stream(queryTimeSlots(userId).asIterable().spliterator(), true)
            .map(VolunteerTimeSlot::new)
            .collect(Collectors.toList());
  }

  private static PreparedQuery queryTimeSlots(String userId) {
    Query query =
            new Query(VOLUNTEER_TIMESLOT_TABLE_NAME)
                    .setFilter(new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId));

    if (datastoreService == null) datastoreService = DatastoreServiceFactory.getDatastoreService();

    return datastoreService.prepare(query);
  }

  public static void deleteAllTimeSlotsByUserId(String userId) {
    datastoreService.delete(
            StreamSupport.stream(queryTimeSlots(userId).asIterable().spliterator(), true)
                    .map(Entity::getKey)
                    .collect(Collectors.toList()));
  }
}
