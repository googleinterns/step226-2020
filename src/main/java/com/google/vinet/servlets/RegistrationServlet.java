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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
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

/**
 * Web Servlet for registering a User with the service, and checking a User's registration status.
 */
@WebServlet("/registration")
public class RegistrationServlet extends HttpServlet {
  /** The minimum allowable length for the "name" attributes of a User. */
  private static final int MIN_NAME_LENGTH = 2;
  /** The maximum allowable length for the "name" attributes of a User. */
  private static final int MAX_NAME_LENGTH = 300;
  /** The Datastore Entity name for the User's information. */
  public static final String USER_TABLE_NAME = "UserInfo";
  /** The homepage for the Isolate user group. */
  public static final String ISOLATE_HOME_PAGE = "/isolate/home.html";
  /** The homepage for the Volunteer user group. */
  public static final String VOLUNTEER_HOME_PAGE = "/volunteer/home.html";
  
  private enum UserType {
    ISOLATE,
    VOLUNTEER
  }

  /** The UserService implementation that this RegistrationServlet depends on. */
  private UserService userService;
  /** The UserService implementation that this RegistrationServlet depends on. */
  private DatastoreService datastore;

  /**
   * Construct a Registrationservlet with its dependencies set to their default implementations.
   */
  public RegistrationServlet() {
    this.userService = UserServiceFactory.getUserService();
    this.datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /**
   * Set the UserService implementation that this RegistrationServlet will depend on.
   * @param userService The UserService implementation to be used.
   */
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  /**
   * Set the DatastoreService implementation that this RegistrationServlet will depend on.
   * @param datastore The DatastoreService implementation to be used.
   */
  public void setDatastore(DatastoreService datastore) {
    this.datastore  = datastore;
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

  /** Returns whether the logged-in User has registered with the service. */
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

    final boolean registered;
    try {
      registered = isUserRegistered();
    } catch (RuntimeException ex) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    final Gson gson = new Gson();

    try {
      response.getWriter().println(gson.toJson(registered));
    } catch (Exception ex) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * @return true, if the currently logged in User is registered.
   */
  public boolean isUserRegistered()  {
    final PreparedQuery preparedQuery = getUserQuery();
    
    final Entity userEntity = getSingleEntity(preparedQuery);

    /* If the query returns a non-null value, then the User is registered.
     * If the query returns a null value, then the user is not registered.
     */
    final boolean registered = (userEntity != null);

    return registered;
  }

  /** @return true, if the currently logged in User is registered as an Isolate. */
  public boolean isUserIsolate()  {
    final UserType type = getUserType();

    return type == UserType.ISOLATE;
  }

  /** @return true, if the currently logged in User is registered as an Volunteer. */
  public boolean isUserVolunteer()  {
    final UserType type = getUserType();

    return type == UserType.VOLUNTEER;
  }

  /** @return the UserType of the currently logged in User. */
  public UserType getUserType()  {
    final PreparedQuery preparedQuery = getUserQuery();

    final Entity userEntity = getSingleEntity(preparedQuery);

    if (userEntity == null) {
      throw new IllegalStateException("cannot check type of unregistered user");
    }

    final String typeString = (String) userEntity.getProperty("type");

    if (typeString == null) {
      throw new IllegalStateException("query did not return a valid user");
    }

    final UserType type = UserType.valueOf(typeString.toUpperCase());

    return type;
  }

  /**
   * Get the single result of executing the provided PreparedQuery.
   * @param preparedQuery The PreparedQuery to execute.
   * @return The single result of executing the provided PreparedQuery.
   */
  public Entity getSingleEntity(PreparedQuery preparedQuery)  {
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
    } catch (TooManyResultsException exception) {
      throw exception;
    } catch (IllegalStateException exception) {
      throw exception;
    }

    return entity;
  }

  /** @return a PreparedQuery for accessing the currently logged in User's information. */
  public PreparedQuery getUserQuery()  {
    final User user = userService.getCurrentUser();
    final String userId = user.getUserId();

    if (userId == null) {
      throw new IllegalStateException("current user does not have an id");
    }

    /* Create a query which will return all entries in the DataStore with a userId matching that of
     *the current user. */
    final Filter userIdFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
    final Query query = new Query(USER_TABLE_NAME).setFilter(userIdFilter);

    final PreparedQuery preparedQuery = datastore.prepare(query);

    return preparedQuery;
  }

  /**
   * Wrapper around {@code KeyFactory.keyToString(String s)}, used to allow stubbing during tests.
   *
   * @param s The String to pass to {@code KeyFactory.keyToString(String s)}.
   */
  protected Key stringToKey(String s) {
    return KeyFactory.stringToKey(s);
  }
}
