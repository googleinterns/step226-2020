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

package com.google.vinet.servlets;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.vinet.data.Volunteer;
import com.google.vinet.data.VolunteerTimeSlot;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;

@WebServlet("/volunteer-availability")
public class VolunteerAvailabilityServlet extends HttpServlet {

  private UserService userService = UserServiceFactory.getUserService();
  private RegistrationServlet registrationServlet = new RegistrationServlet();

  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  public void setRegistrationServlet(RegistrationServlet registrationServlet) {
    this.registrationServlet = registrationServlet;
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws IOException {
    if (response == null) throw new IllegalArgumentException("response must not be null");

    if (request == null) throw new IllegalArgumentException("request must not be null");

    if (!userService.isUserLoggedIn()) {
      response.sendError(
              HttpServletResponse.SC_UNAUTHORIZED, "user must be logged in to post a request");
      return;
    }

    final boolean registered = registrationServlet.isUserRegistered();

    if (!registered) {
      response.sendError(
              HttpServletResponse.SC_UNAUTHORIZED, "user must be registered to submit availability");
      return;
    }

    final boolean isVolunteer = registrationServlet.isUserVolunteer();

    if (!isVolunteer) {
      response.sendError(
              HttpServletResponse.SC_UNAUTHORIZED,
              "user must be registered as a volunteer to submit availability");
      return;
    }

    final User user = userService.getCurrentUser();
    if (user == null) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "user is null!");
      return;
    }

    final String userId = user.getUserId();
    if (userId == null) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "user id is null!");
      return;
    }
    final Volunteer volunteer = new Volunteer(userId);

    final Map<String, String[]> parameterMap = request.getParameterMap();
    if (parameterMap == null || parameterMap.isEmpty()) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no parameters provided!");
      return;
    }

    final String[] startTimes = parameterMap.get("ISO-start-time");
    if (startTimes == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "start times not provided!");
      return;
    }

    final String[] endTimes = parameterMap.get("ISO-end-time");
    if (endTimes == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "end times not provided!");
      return;
    }

    // Delete existing timeslots before storing new list of them.
    VolunteerTimeSlot.deleteAllTimeSlotsByUserId(userId);

    // Loop through all parameters and create each slot object
    for (int i = 0; i < (Math.min(startTimes.length, endTimes.length)); i++) {
      String startTime = startTimes[i];
      String endTime = endTimes[i];

      try {
        Instant startInstant = Instant.parse(startTime);
        Instant endInstant = Instant.parse(endTime);
        new VolunteerTimeSlot(startInstant, endInstant, volunteer).toDatastore();
      } catch (NullPointerException e) {
        System.err.println("Volunteer timeslot times are null!");
      } catch (IllegalArgumentException | DateTimeParseException e) {
        System.err.println("Error parsing volunteer timeslot times!");
      }
    }

    response.sendRedirect("volunteer/availability.jsp");
  }

  /**
   * Get all available time slots for logged-in volunteer.
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws IOException {
    if (response == null) throw new IllegalArgumentException("response must not be null");

    if (request == null) throw new IllegalArgumentException("request must not be null");

    if (!userService.isUserLoggedIn()) {
      response.sendError(
              HttpServletResponse.SC_UNAUTHORIZED, "user must be logged in to get availability");
      return;
    }

    final boolean registered = registrationServlet.isUserRegistered();

    if (!registered) {
      response.sendError(
              HttpServletResponse.SC_UNAUTHORIZED, "user must be registered to get availability");
      return;
    }

    final boolean isVolunteer = registrationServlet.isUserVolunteer();

    if (!isVolunteer) {
      response.sendError(
              HttpServletResponse.SC_UNAUTHORIZED,
              "user must be registered as a volunteer to get availability");
      return;
    }

    final User user = userService.getCurrentUser();
    if (user == null) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "user is null!");
      return;
    }

    final String userId = user.getUserId();
    if (userId == null) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "userId is null!");
      return;
    }

    // Run a query to get all slot stored for logged-in volunteer
    response.setContentType("application/json;");
    new Gson().toJson(VolunteerTimeSlot.getTimeslotsByUserId(userId), response.getWriter());
  }
}
