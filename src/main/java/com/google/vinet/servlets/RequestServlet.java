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
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.vinet.data.Isolate;
import com.google.vinet.data.IsolateTimeSlot;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.Stream;

@WebServlet("/request")
public class RequestServlet extends HttpServlet {
  public static final String TICKET_TABLE_NAME = "Ticket";
  private final DatastoreService datastore;
  private final UserService userService;

  public RequestServlet() {
    this.datastore = DatastoreServiceFactory.getDatastoreService();
    this.userService = UserServiceFactory.getUserService();
  }

  public RequestServlet(DatastoreService datastore, UserService userService) {
    this.datastore = datastore;
    this.userService = userService;
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

    final boolean registered = RegistrationServlet.isUserRegistered(this.userService);

    if (!registered) {
      response.sendError(
          HttpServletResponse.SC_UNAUTHORIZED,
          "user must be registered to post a request"
      );
      return;
    }

    final boolean isIsolate = RegistrationServlet.isUserIsolate(this.userService);

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
    String timezoneOffset = request.getParameter("timezoneOffset");
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

    if (timezoneOffset == null) {
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
    timezoneOffset = timezoneOffset.trim();
    subjects = trimMembers(subjects);
    details = trimMembers(details);

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

    if (timezoneOffset.equals("")) {
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
    }

    startTime = String.format("%sT%s:00%s", date, startTime, timezoneOffset);
    endTime = String.format("%sT%s:00%s", date, endTime, timezoneOffset);

    final Instant start;
    final Instant end;
    final Isolate isolate;
    final IsolateTimeSlot isolateTimeSlot;

    try{
      DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME();
      start = Instant.from(formatter.parse(startTime));
      end = Instant.from(formatter.parse(endTime));
      isolate = new Isolate(userId);
      isolateTimeSlot = new IsolateTimeSlot(start, end , isolate);
    } catch (DateTimeParseException exception) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "error parsing timeslot");
      return;
    }

    final Gson gson = new Gson();

    final Entity ticketEntity = new Entity("Ticket");
    ticketEntity.setProperty("isolateId", userId);
    ticketEntity.setProperty("volunteerId", null);
    ticketEntity.setProperty("timeslot", isolateTimeSlot);
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

    final boolean registered = RegistrationServlet.isUserRegistered(this.userService);

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
    }

    keyString = keyString.trim();

    if (keyString.equals("")) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }


    /* An IllegalArgumentException is thrown when the keyString cannot be parsed.
     */
    final Key key;
    try {
      key = KeyFactory.stringToKey(keyString);
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
      requestEntity = datastore.get(key);
    } catch (EntityNotFoundException | IllegalArgumentException exception) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    } catch (DatastoreFailureException exception) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      throw exception;
    }

    if (requestEntity == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  /**
   * Apply the trim() method to all non-null members of a String array,
   * in-place.
   * @param array The array to process.
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
