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

import java.time.Instant;

public class VolunteerTimeSlot extends TimeSlot implements Datastoreable {
  private DatastoreService datastoreService;
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
    entity.setProperty("start", getStart());
    entity.setProperty("end", getEnd());

    datastoreService.put(entity);
  }
}
