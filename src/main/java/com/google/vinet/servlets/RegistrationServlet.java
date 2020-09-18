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
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@WebServlet("/registration")
public class RegistrationServlet extends HttpServlet {

  private static final int MIN_NAME_LENGTH = 2;
  private static final int MAX_NAME_LENGTH = 300;
  private static final String USER_TABLE_NAME = "UserInfo";
  public static final String ISOLATE_HOME_PAGE = "/isolate/home.html";
  public static final String VOLUNTEER_HOME_PAGE = "/volunteer/home.html";

  private enum UserType {
    ISOLATE,
    VOLUNTEER
  }

  private UserService userService;

  public RegistrationServlet() {
    this.userService = UserServiceFactory.getUserService();
  }

  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    if (request == null) return;

    if (!userService.isUserLoggedIn()) {
      System.err.println("User is not logged in!");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    try {
      final String userId = userService.getCurrentUser().getUserId();
      if (userId == null) {
        System.err.println("User id is null!");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      // Setup variables before verifying and validating input
      UserType userType = null;
      final Set<String> propertyNames =
              Sets.newHashSet("firstname", "lastname", "type", "latitude", "longitude");
      final Map<String, String[]> parameterMap = request.getParameterMap();

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Entity entity = new Entity(USER_TABLE_NAME, userId);
      entity.setProperty("userId", userId);

      for (String propertyName : propertyNames) {
        final String[] parameterArray = parameterMap.get(propertyName);
        if (parameterArray == null || parameterArray.length < 1) {
          System.err.printf("Missing parameter %s for registration request!\n", propertyName);
          return;
        }
        // We only need the first value for this parameter. There shouldn't be any more.
        String parameter = parameterArray[0];
        if (parameter == null) {
          System.err.printf("Parameter %s is null!", propertyName);
          return;
        }
        // Make sure parameter value is not blank
        parameter = parameter.trim();
        if (parameter.isEmpty()) {
          System.err.printf("Parameter %s is blank!", propertyName);
          return;
        }

        Object value = null; // The value to be stored

        // Check all constraints
        if (propertyName.equals("firstname") || propertyName.equals("lastname")) {
          final int length = parameter.length();
          if (length < MIN_NAME_LENGTH || length > MAX_NAME_LENGTH) {
            System.err.printf("Parameter %s is not within length bounds!\n", propertyName);
            return;
          }
        }

        if (propertyName.equals("type")) {
          try {
            userType = UserType.valueOf(parameter.toUpperCase());
            value = userType.toString(); // store the parsed string
          } catch (IllegalArgumentException e) {
            System.err.println("Wrong type parameter!");
            return;
          }
        }

        if (propertyName.equals("latitude") || propertyName.equals("longitude")) {
          try {
            value = Double.parseDouble(parameter);
          } catch (NumberFormatException e) {
            System.err.printf("The %s is not formatted as a double!", propertyName);
            return;
          }
        }

        if (value == null) value = parameter;
        entity.setProperty(propertyName, value);
      }

      datastore.put(entity);

      response.setStatus(HttpServletResponse.SC_OK);

      if (userType == UserType.ISOLATE) {
        response.sendRedirect(ISOLATE_HOME_PAGE);
      } else if (userType == UserType.VOLUNTEER) {
        response.sendRedirect(VOLUNTEER_HOME_PAGE);
      } else {
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      }
    } catch (Exception e) {
      System.err.println("There was an error registering!");
      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Returns whether the logged-in User has registered with the service.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (response == null) {
      throw new IllegalArgumentException("response cannot be null");
    }

    if (request == null) {
      throw new IllegalArgumentException("request cannot be null");
    }

    if (!this.userService.isUserLoggedIn()) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    final User user = userService.getCurrentUser();
    final String userId = user.getUserId();

    if (userId == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    /* Create a query which will return all entries in the DataStore with a userId matching that of
     *the current user. */
    final Filter userIdFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
    final Query query = new Query(USER_TABLE_NAME).setFilter(userIdFilter);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    final PreparedQuery preparedQuery = datastore.prepare(query);

    final Entity entity;

    /* Try to retrieve the results of the query as a single Entity.
     * If there is more than a single result, then the registration service has incorrectly
     * registered a user twice. This error will be passed up to the caller.
     *
     * In a production environment, this would have to be reported to the sysadmin/project owner,
     * as duplicate registration should never be allowed.
     */
    try {
      entity = preparedQuery.asSingleEntity();
    } catch (TooManyResultsException | IllegalStateException exception) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      throw exception;
    }

    /* If the query returns a non-null value, then the User is registered.
     * If the query returns a null value, then the user is not registered.
     */
    final boolean registered = (entity != null);

    final Gson gson = new Gson();

    try {
      response.getWriter().println(gson.toJson(registered));
    } catch (Exception ex) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
