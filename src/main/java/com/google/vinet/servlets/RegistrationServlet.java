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
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.Sets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;

@WebServlet("/registration")
public class RegistrationServlet extends HttpServlet {

  private static final int MIN_NAME_LENGTH = 2;
  private static final int MAX_NAME_LENGTH = 300;
  private static final String USER_TABLE_NAME = "UserInfo";

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

      UserType userType = null;
      final Set<String> propertyNames =
              Sets.newHashSet("firstname", "lastname", "type", "latitude", "longitude");
      final Map<String, String[]> parameterMap = request.getParameterMap();

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Entity entity = new Entity(USER_TABLE_NAME, userId);
      entity.setProperty("userId", userId);

      for (String propertyName : propertyNames) {
        final String[] parameterArray;
        if (!parameterMap.containsKey(propertyName) || (parameterArray = parameterMap.get(propertyName)).length < 1) {
          System.err.printf("Missing parameter %s for registration request!\n", propertyName);
          return;
        }
        // Get first (and only) value of each parameter
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
        response.sendRedirect("/isolate/home.html");
      } else if (userType == UserType.VOLUNTEER) {
        response.sendRedirect("/volunteer/home.html");
      } else {
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      }
    } catch (Exception e) {
      System.err.println("There was an error registering!");
      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }
  }
}
