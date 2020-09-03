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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;

import java.time.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.stream.Stream;

@WebServlet("/request")
public class RequestServlet extends HttpServlet {
  public static final String TICKET_TABLE_NAME = "Ticket";
  private final DatastoreService datastore;
  private final UserService userService;
  private final RegistrationServlet registrationServlet;

  public RequestServlet() {
    this.datastore = DatastoreServiceFactory.getDatastoreService();
    this.userService = UserServiceFactory.getUserService();
    this.registrationServlet = new RegistrationServlet();
  }

  public RequestServlet(DatastoreService datastore, UserService userService, RegistrationServlet registrationServlet) {
    this.datastore = datastore;
    this.userService = userService;
    this.registrationServlet = registrationServlet;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    if (response == null) {
      throw new IllegalArgumentException("response must not be null");
    }

    if (request == null) {
      throw new IllegalArgumentException("request must not be null");
    }

    if (!this.userService.isUserLoggedIn()) {
      response.sendError(
          HttpServletResponse.SC_UNAUTHORIZED,
          "user must be logged in to post a request"
      );
      return;
    }

    final boolean registered = registrationServlet.isUserRegistered(this.userService);

    if (!registered) {
      response.sendError(
          HttpServletResponse.SC_UNAUTHORIZED,
          "user must be registered to post a request"
      );
      return;
    }

    final boolean isIsolate = registrationServlet.isUserIsolate(this.userService);

    if (!isIsolate) {
      response.sendError(
          HttpServletResponse.SC_UNAUTHORIZED,
          "user must be registered as an isolate to post a request"
      );
      return;
    }

    String date = request.getParameter("date");
    String duration = request.getParameter("duration");
    String startTime = request.getParameter("startTime");
    String endTime = request.getParameter("endTime");
    String timezone = request.getParameter("timezone");
    String[] subjects = request.getParameterValues("subject");
    String[] details = request.getParameterValues("details");

    if (date == null) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "date must not be null"
      );
      return;
    }

    if (duration == null) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "duration must not be null"
      );
      return;
    }

    if (startTime == null) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "startTime must not be null"
      );
      return;
    }

    if (endTime == null) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "endTime must not be null"
      );
      return;
    }

    if (timezone == null) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "timezoneOffset must not be null"
      );
      return;
    }

    if (subjects == null || subjects.length < 1) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "subjects array must not be null or empty"
      );
      return;
    }

    if (details == null || details.length < 1) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "details array must not be null or empty"
      );
      return;
    }

    date = date.trim();
    duration = duration.trim();
    startTime = startTime.trim();
    endTime = endTime.trim();
    timezone = timezone.trim();
    trimMembers(subjects);
    trimMembers(details);

    if (date.equals("")) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "date must not be empty"
      );
      return;
    }

    if (duration.equals("")) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "duration must not be empty"
      );
      return;
    }

    if (startTime.equals("")) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "startTime must not be empty"
      );
      return;
    }

    if (endTime.equals("")) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "endTime must not be empty"
      );
      return;
    }

    if (timezone.equals("")) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "timezoneOffset must not be empty"
      );
      return;
    }

    if (Stream.of(subjects).anyMatch(e -> e == null || e.equals(""))) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "all members of subjects array must not be null or empty"
      );
      return;
    }

    if (Stream.of(details).anyMatch(e -> e == null || e.equals(""))) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "all members of details array must not be null or empty"
      );
      return;
    }

    if (subjects.length != details.length) {
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "subjects and details must be of equal length"
      );
      return;
    }

    /* This catch block is used to ensure UserService has not become unavailable
     * since login status was confirmed. This should never happen, but could. */
    final String userId;
    try {
      userId = userService.getCurrentUser().getUserId();
    } catch (Exception ex) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    /*
     * The following is an assurance that the provided startTime and endTime are valid.
     * The computed Instant's and IsolateTimeSlot will not be stored in Datastore, as
     * this is not supported.
     * Instead, the startTime and endTime are stored.
     */

    final ZoneOffset timezoneOffset;
    try {
      timezoneOffset = ZoneOffset.of(timezone);
    } catch (DateTimeException exception) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "error parsing timezone");
      return;
    }


    final LocalDate day;
    try{
      day = LocalDate.parse(date);
    } catch (DateTimeParseException exception) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "error parsing date");
      return;
    }

    final LocalTime start;
    try{
      start = LocalTime.parse(startTime);
    } catch (DateTimeParseException exception) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "error parsing startTime");
      return;
    }

    final LocalTime end;
    try{
      end = LocalTime.parse(endTime);
    } catch (DateTimeParseException exception) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "error parsing endTime");
      return;
    }

    final Duration requestDuration;
    try{
      requestDuration = Duration.ofMinutes(Long.parseLong(duration));
    } catch (ArithmeticException | NumberFormatException exception) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "error parsing duration");
      return;
    }

    final OffsetDateTime startDateTime = OffsetDateTime.of(day, start, timezoneOffset);
    final OffsetDateTime endDateTime = OffsetDateTime.of(day, end, timezoneOffset);

    final Instant startInstant = startDateTime.toInstant();
    final Instant endInstant = endDateTime.toInstant();

    final Gson gson = new Gson();

    final Entity ticketEntity = new Entity(TICKET_TABLE_NAME);
    ticketEntity.setProperty("isolateId", userId);
    ticketEntity.setProperty("volunteerId", null);
    ticketEntity.setProperty("date", day.toString());
    ticketEntity.setProperty("duration", requestDuration.toString());
    ticketEntity.setProperty("startTime", startInstant.toString());
    ticketEntity.setProperty("endTime", endInstant.toString());
    ticketEntity.setProperty("subjects", gson.toJson(subjects));
    ticketEntity.setProperty("details", gson.toJson(details));

    this.datastore.put(ticketEntity);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
    if (response == null) {
      throw new IllegalArgumentException("response must not be null");
    }

    if (request == null) {
      throw new IllegalArgumentException("request must not be null");
    }

    if (!this.userService.isUserLoggedIn()) {
      response.sendError(
          HttpServletResponse.SC_UNAUTHORIZED,
          "user must be logged in to fetch a request"
      );
      return;
    }

    final boolean registered = registrationServlet.isUserRegistered(this.userService);

    if (!registered) {
      response.sendError(
          HttpServletResponse.SC_UNAUTHORIZED,
          "user must be registered to fetch a request"
      );
      return;
    }
    
    String keyString = request.getParameter("key");

    if (keyString == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    keyString = keyString.trim();

    if (keyString.equals("")) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }


    /* An IllegalArgumentException is thrown when the keyString cannot be parsed.
     */
    final Key key;
    try {
      key = registrationServlet.stringToKey(keyString);
    } catch (IllegalArgumentException exception) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    /*
     * An EntityNotFoundException or IllegalArgumentException imply the client has sent a bad
     * request, which is why these are mapped to SC_BAD_REQUEST.
     * A DataStoreFailureException implies that DataStore has failed, and this is interpreted
     * as a server-side error, therefore SC_INTERNAL_SERVER_ERROR is thrown.
     */
    final Entity requestEntity;
    try{
      requestEntity = this.datastore.get(key);
    } catch (DatastoreFailureException exception) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      throw exception;
    } catch (EntityNotFoundException | IllegalArgumentException exception) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    if (requestEntity == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    try{
      Gson gson = new Gson();
      response.getWriter().println(gson.toJson(requestEntity.getProperties()));
    } catch (Exception exception) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      throw exception;
    }
  }

  /**
   * Apply the trim() method to all non-null members of a String array,
   * in-place.
   * @param array The array to process.
   * @return A reference to {@code array}, for chaining.
   */
  private static String[] trimMembers(String[] array) {
    for (int i = 0; i < array.length; i++) {
      if (array[i] != null) {
        array[i] = array[i].trim();
      }
    }
    return array;
  }
}
