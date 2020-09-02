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
import java.time.format.DateTimeFormatter;
import java.util.Map;

@WebServlet("/volunteer-availability")
public class VolunteerAvailabilityServlet extends HttpServlet {

  private final UserService userService = UserServiceFactory.getUserService();

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

    final boolean registered = RegistrationServlet.isUserRegistered(userService);

    if (!registered) {
      response.sendError(
              HttpServletResponse.SC_UNAUTHORIZED, "user must be registered to submit availability");
      return;
    }

    final boolean isVolunteer = RegistrationServlet.isUserVolunteer(userService);

    if (!isVolunteer) {
      response.sendError(
              HttpServletResponse.SC_UNAUTHORIZED,
              "user must be registered as a volunteer to submit availability");
      return;
    }

    // TODO check if it's a valid userId
    Volunteer volunteer = new Volunteer(userService.getCurrentUser().getUserId());

    // TODO check if any of the parameters are null / not present
    Map<String, String[]> parameterMap = request.getParameterMap();
    String timezoneOffset = parameterMap.get("timezoneOffset")[0];
    timezoneOffset = timezoneOffset.replaceFirst("(.{3})", "$1:"); // insert a colon to get ISO time
    final String[] dates = parameterMap.get("date");
    final String[] startTimes = parameterMap.get("start-time");
    final String[] endTimes = parameterMap.get("end-time");

    final String ISO_FORMAT = "%sT%s:00%s";
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    //TODO don't store duplicated, but update them

    for (int i = 0; i < dates.length; i++) {
      String date = dates[i];
      String startTime = startTimes[i];
      String endTime = endTimes[i];

      Instant startInstant =
              Instant.from(
                      dateTimeFormatter.parse(String.format(ISO_FORMAT, date, startTime, timezoneOffset)));
      Instant endInstant =
              Instant.from(
                      dateTimeFormatter.parse(String.format(ISO_FORMAT, date, endTime, timezoneOffset)));

      VolunteerTimeSlot volunteerTimeSlot =
              new VolunteerTimeSlot(startInstant, endInstant, volunteer);
      volunteerTimeSlot.toDatastore();
    }
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

    final boolean registered = RegistrationServlet.isUserRegistered(userService);

    if (!registered) {
      response.sendError(
              HttpServletResponse.SC_UNAUTHORIZED, "user must be registered to get availability");
      return;
    }

    final boolean isVolunteer = RegistrationServlet.isUserVolunteer(userService);

    if (!isVolunteer) {
      response.sendError(
              HttpServletResponse.SC_UNAUTHORIZED,
              "user must be registered as a volunteer to get availability");
      return;
    }

    // Run a query to get all slot stored for logged-in volunteer
    response.setContentType("application/json;");
    new Gson()
            .toJson(
                    VolunteerTimeSlot.getTimeslotsByUserId(userService.getCurrentUser().getUserId()),
                    response.getWriter());
  }
}
