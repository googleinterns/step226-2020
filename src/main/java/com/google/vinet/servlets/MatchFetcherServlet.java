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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.vinet.data.Match;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Servlet for getting matches for logged-in user
 */
@WebServlet("/match-fetcher")
public class MatchFetcherServlet extends HttpServlet {
  private final UserService userService = UserServiceFactory.getUserService();
  private final RegistrationServlet registrationServlet = new RegistrationServlet();
  private final DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws IOException {
    if (response == null) throw new IllegalArgumentException("response must not be null");

    if (request == null) throw new IllegalArgumentException("request must not be null");

    if (!userService.isUserLoggedIn()) {
      response.sendError(
              HttpServletResponse.SC_UNAUTHORIZED, "user must be logged in to get matches");
      return;
    }

    final boolean registered = registrationServlet.isUserRegistered(userService);

    if (!registered) {
      response.sendError(
              HttpServletResponse.SC_UNAUTHORIZED, "user must be registered to get matches");
      return;
    }

    final String userId = userService.getCurrentUser().getUserId();
    final boolean isVolunteer = registrationServlet.isUserVolunteer(userService);
    final String idFilterProperty = isVolunteer ? "volunteerId" : "isolateId";


    // Run a query to get all matches for this user
    Query query =
            new Query("Matching") // TODO make into constant
                    .setFilter(
                            new Query.FilterPredicate(idFilterProperty, Query.FilterOperator.EQUAL, userId));

    PreparedQuery pq = datastoreService.prepare(query);
    final List<Match> matches =
            StreamSupport.stream(pq.asIterable().spliterator(), true)
                    .map(match -> new Match(match, isVolunteer))
                    .collect(Collectors.toList());

    System.out.println("userId " + userId + " isvolunteer " + isVolunteer + " matches #: " + matches.size());

    response.setContentType("application/json;");
    new Gson().toJson(matches, response.getWriter());
  }
}
