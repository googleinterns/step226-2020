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

package com.google.vinet.servlets;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.*;
import com.google.gson.*;
import com.google.vinet.data.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

@WebServlet("/fetch-requests")
public class IsolateRequestServlet  extends HttpServlet {
  public DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  public UserService userService = UserServiceFactory.getUserService();
  public RegistrationServlet registrationServlet = new RegistrationServlet();

  /**
   * Fetch the current user's tickets from DataStore, and return them as JSON, sorted in descending
   * order on the basis of their date.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (response == null) {
      throw new IllegalArgumentException("response must not be null");
    }

    if (request == null) {
      throw new IllegalArgumentException("request must not be null");
    }

    if (!this.userService.isUserLoggedIn()) {
      response.sendError(
          HttpServletResponse.SC_UNAUTHORIZED, "user must be logged in to fetch a request");
      return;
    }

    final boolean registered = registrationServlet.isUserRegistered();

    if (!registered) {
      response.sendError(
          HttpServletResponse.SC_UNAUTHORIZED, "user must be registered to fetch a request");
      return;
    }

    final String userId = userService.getCurrentUser().getUserId();

    final Query query = new Query(IsolateTimeSlot.ISOLATE_TIME_SLOT_TABLE_NAME);
    final Query.Filter filter = new Query.FilterPredicate("isolateId", Query.FilterOperator.EQUAL, userId);
    query.setFilter(filter).addSort("date", Query.SortDirection.DESCENDING);

    final PreparedQuery results = datastore.prepare(query);

    Gson gson = new Gson();

    List<IsolateRequest> isolateRequests = new LinkedList<>();

    /* An EntityNotFoundException implies the system has stored an isolate's request, but failed to link
     * that request to the correct Ticket entity in Datastore. The exception must be thrown to allow Google
     * Cloud Console to observe and log it. It should not be caught by the caller. */
    try {
      for (Entity entity : results.asIterable()) {
        final String date = (String) entity.getProperty("date");
        final String start = (String) entity.getProperty("startTime");
        final String end = (String) entity.getProperty("endTime");

        final Key ticketKey = KeyFactory.stringToKey((String) entity.getProperty("ticketKey"));
        final Entity ticket = datastore.get(ticketKey);

        final String[] subjects = gson.fromJson((String) ticket.getProperty("subjects"), String[].class);
        final String[] details = gson.fromJson((String) ticket.getProperty("details"), String[].class);

        final IsolateRequest isolateRequest = new IsolateRequest(date, start, end, subjects, details);
        isolateRequests.add(isolateRequest);
      }
    } catch (EntityNotFoundException exception) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      throw new RuntimeException(exception);
    }

    try {
      /* Converting the Isolate's Requests from a LinkedList to an array was found to solve an error
       * with Gson following circular references, and causing a StackOverflowError to be thrown. */
      response.getWriter().println(gson.toJson(isolateRequests.toArray(new IsolateRequest[] {})));
    } catch (Exception exception) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  public static class IsolateRequest {
    private final String date, start, end;
    private final String[] subjects, details;

    public IsolateRequest(String date, String start, String end, String[] subjects, String[] details) {
      this.date = date;
      this.start = start;
      this.end = end;
      this.subjects = subjects;
      this.details = details;
    }
  }
}
