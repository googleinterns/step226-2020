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
import com.google.gson.Gson;

/** To represent a match for displaying to the user */
public class Match {
  public DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  protected final String date, start, end, firstName, lastName;
  protected final String[] subjects, details;

  public Match(
      String date,
      String start,
      String end,
      String firstName,
      String lastName,
      String[] subjects,
      String[] details) {
    this.date = date;
    this.start = start;
    this.end = end;
    this.firstName = firstName;
    this.lastName = lastName;
    this.subjects = subjects;
    this.details = details;
  }

  public Match(Entity matchingEntity, boolean isVolunteer) {
    String isolateId = (String) matchingEntity.getProperty("isolateId");
    String volunteerId = (String) matchingEntity.getProperty("volunteerId");
    String ticketKey = (String) matchingEntity.getProperty("ticketKey");

    // Get opposing-user's name
    String userId = isVolunteer ? isolateId : volunteerId;

    Entity user;
    try {
      user =
          datastore
              .prepare(
                  new Query("UserInfo") // TODO use constant instead
                      .setFilter(
                          new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId)))
              .asSingleEntity();
    } catch (PreparedQuery.TooManyResultsException exception) {
      /* Since a TooManyResultsException implies a user has registered more than once, the exception
       * must be thrown to allow Google Cloud Console to observe and log it. It should not be
       * caught by the caller. */
      throw exception;
    }

    assert(user != null);

    // Get ticket
    PreparedQuery query =
        datastore.prepare(
            new Query(Isolate.TICKET_TABLE_NAME)
                .setFilter(
                    new Query.FilterPredicate("isolateId", Query.FilterOperator.EQUAL, isolateId)));

    Entity ticket = query.asSingleEntity(); // TODO check in case missing or duplicate

    date = (String) matchingEntity.getProperty("date");
    start = (String) matchingEntity.getProperty("start");
    end = (String) matchingEntity.getProperty("end");
    firstName = (String) user.getProperty("firstname");
    lastName = (String) user.getProperty("lastname");
    subjects = new Gson().fromJson((String) ticket.getProperty("subjects"), String[].class);
    details = new Gson().fromJson((String) ticket.getProperty("details"), String[].class);
  }

  public String getDate() {
    return date;
  }

  public String getStart() {
    return start;
  }

  public String getEnd() {
    return end;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String[] getSubjects() {
    return subjects;
  }

  public String[] getDetails() {
    return details;
  }
}
