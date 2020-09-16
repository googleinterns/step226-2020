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
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.*;

public class IsolateTimeSlot extends TimeSlot implements Datastoreable {
  /** The Datastore Entity name for an IsolateTimeSlot. */
  protected static final String ISOLATE_TIME_SLOT_TABLE_NAME = "IsolateTimeSlot";
  /** The ticket associated with this TimeSlot. */
  protected final Key ticket;
  /** The date this IsolateTimeSlot is scheduled on. */
  protected final LocalDate date;
  /** The DatastoreService implementation to depend on. */
  public static DatastoreService datastore;

  /**
   * Construct an IsolateTimeSlot using the properties of the provided Datastore {@code Entity}
   *
   * <p>The {@code Entity} must have these properties:
   *
   * <table>
   *      <thead>
   *          <th>
   *              Property
   *          </th>
   *          <th>
   *              Type
   *          </th>
   *          <th>
   *              Description
   *          </th>
   *      </thead>
   *      <tr>
   *          <td>
   *              startTime
   *          </td>
   *          <td>
   *              {@link java.lang.String}
   *          </td>
   *          <td>
   *              The start time of the delivry window.
   *          </td>
   *      </tr>
   *      <tr>
   *          <td>
   *              endTime
   *          </td>
   *          <td>
   *              {@link java.lang.String}
   *          </td>
   *          <td>
   *              The end time of the delivery window.
   *          </td>
   *      </tr>
   *      <tr>
   *          <td>
   *              isolateId
   *          </td>
   *          <td>
   *              {@link java.lang.String}
   *          </td>
   *         <td>
   *             The id of this Isolate as per the {@code UserService} API.
   *         </td>
   *      </tr>
   *      <tr>
   *          <td>
   *              date
   *          </td>
   *          <td>
   *              {@link java.lang.String}
   *          </td>
   *          <td>
   *              The date of delivery. <b>Note: This property must be encoded as a valid ISO date string.</b>
   *          </td>
   *      </tr>
   *      <tr>
   *          <td>
   *              ticketKey
   *          </td>
   *          <td>
   *              {@link com.google.appengine.api.datastore.Key}
   *          </td>
   *          The Datastore key of the ticket associated with this IsolateTimeSlot.
   *      </tr>
   *  </table>
   *
   * @param entity The entity to pull data from in order to construct this IsolateTimeSlot.
   * @see <a
   *     href="https://cloud.google.com/appengine/docs/standard/java/javadoc/com/google/appengine/api/datastore/Entity.html">
   *     The Datastore documentation for the {@code Entity} class.</a>
   */
  public IsolateTimeSlot(Entity entity) {
    this(
        Instant.parse((String) entity.getProperty("startTime")),
        Instant.parse((String) entity.getProperty("endTime")),
        new Isolate((String) entity.getProperty("isolateId")),
        LocalDate.parse((String) entity.getProperty("date")),
        KeyFactory.stringToKey((String) entity.getProperty("ticketKey")));
  }

  /**
   * Construct an IsolateTimeSlot.
   *
   * @param start The start of the TimeSlot.
   * @param end The end of the TimeSlot.
   * @param isolate The isolate associated with the TimeSlot.
   * @param date The date on which the TimeSlot is scheduled.
   * @param ticket The Datastore key of the request ticket for this TimeSlot.
   */
  public IsolateTimeSlot(Instant start, Instant end, Isolate isolate, LocalDate date, Key ticket) {
    super(start, end, isolate);
    this.date = date;
    this.ticket = ticket;
  }

  public Isolate getIsolate() {
    return (Isolate) registeredUser;
  }

  public static List<IsolateTimeSlot> getTimeslotsByUserId(String userId) {
    return StreamSupport.stream(queryTimeSlots(userId).asIterable().spliterator(), true)
        .map(IsolateTimeSlot::new)
        .collect(Collectors.toList());
  }

  private static PreparedQuery queryTimeSlots(String userId) {
    Query query =
        new Query(ISOLATE_TIME_SLOT_TABLE_NAME)
            .setFilter(new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId));

    return datastore.prepare(query);
  }

  /** Put this IsolateTimeSlot into Datastore. */
  @Override
  public void toDatastore() {
    /* TODO: Check that all instance variables are non-null before posting to Datastore. */
    final Entity entity = new Entity(ISOLATE_TIME_SLOT_TABLE_NAME);
    entity.setProperty("ticketKey", KeyFactory.keyToString(ticket));
    entity.setProperty("isolateId", this.getIsolate().userId);
    entity.setProperty("date", date.toString());
    entity.setProperty("startTime", start.toString());
    entity.setProperty("endTime", end.toString());

    datastore.put(entity);
  }
}
