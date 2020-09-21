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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Entity;
import java.time.Instant;
import java.time.LocalDate;

public class IsolateTimeSlot extends TimeSlot implements Datastoreable{
  protected final static String ISOLATE_TIME_SLOT_TABLE_NAME = "IsolateTimeSlot";

  protected final Key ticket;
  protected final LocalDate date;
  protected final DatastoreService datastore;

  /**
   * Construct an IsolateTimeSlot with a dependency on the default implementation of Datastore.
   * @param start The start of the TimeSlot.
   * @param end The end of the TimeSlot.
   * @param isolate The isolate associated with the TimeSlot.
   * @param date The date on which the TimeSlot is scheduled.
   * @param ticket The Datastore key of the request ticket for this TimeSlot.
   */
  public IsolateTimeSlot(Instant start, Instant end, Isolate isolate, LocalDate date, Key ticket) {
    this(start, end, isolate, date, ticket, DatastoreServiceFactory.getDatastoreService());
  }

  /**
   Construct an IsolateTimeSlot with a dependency on the provided implementation of Datastore.
   * @param start The start of the TimeSlot.
   * @param end The end of the TimeSlot.
   * @param isolate The isolate associated with the TimeSlot.
   * @param date The date on which the TimeSlot is scheduled.
   * @param ticket The Datastore key of the request ticket for this TimeSlot.
   * @param datastore The implementation of Datastore to depend on.
   */
  public IsolateTimeSlot(Instant start, Instant end, Isolate isolate, LocalDate date, Key ticket, DatastoreService datastore) {
    super(start, end, isolate);
    this.date = date;
    this.ticket = ticket;
    this.datastore = datastore;
  }

  public Isolate getIsolate() {
    return (Isolate) registeredUser;
  }

  /**
   * Put this IsolateTimeSlot into Datstore.
   */
  @Override
  public void toDatastore() {
    /* TODO: Check that all instance variables are non-null before posting to Datastore. */
    final  Entity entity = new Entity(ISOLATE_TIME_SLOT_TABLE_NAME);
    entity.setProperty("ticketKey", KeyFactory.keyToString(ticket));
    entity.setProperty("isolateId", this.getIsolate().userId);
    entity.setProperty("date", date.toString());
    entity.setProperty("startTime", start.toString());
    entity.setProperty("endTime", end.toString());

    this.datastore.put(entity);
  }
}
