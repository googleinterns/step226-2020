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
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.vinet.data.Match;
import com.google.vinet.data.MatchingRunner;

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
  private UserService userService = UserServiceFactory.getUserService();
  private RegistrationServlet registrationServlet = new RegistrationServlet();
  private final DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  public void setRegistrationServlet(RegistrationServlet registrationServlet) {
    this.registrationServlet = registrationServlet;
  }

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

    final boolean registered = registrationServlet.isUserRegistered();

    if (!registered) {
      response.sendError(
              HttpServletResponse.SC_UNAUTHORIZED, "user must be registered to get matches");
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

    final boolean isVolunteer = registrationServlet.isUserVolunteer();
    final String idFilterProperty = isVolunteer ? "volunteerId" : "isolateId";

    // Run a query to get all matches for this user
    Query query =
            new Query(MatchingRunner.MATCHING_TABLE_NAME)
                    .setFilter(
                            new Query.FilterPredicate(idFilterProperty, Query.FilterOperator.EQUAL, userId));

    PreparedQuery pq = datastoreService.prepare(query);
    final List<Match> matches =
            StreamSupport.stream(pq.asIterable().spliterator(), true)
                    .map(match -> new Match(match, isVolunteer))
                    .collect(Collectors.toList());

    response.setContentType("application/json;");
    new Gson().toJson(matches, response.getWriter());
  }
}
