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
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VolunteerAvailabilityServletTest {
  @Mock
  UserService userService;
  @Mock
  HttpServletRequest request;
  @Mock
  HttpServletResponse response;
  @Mock
  User user;
  @Mock
  RegistrationServlet registrationServlet;
  @InjectMocks
  VolunteerAvailabilityServlet volunteerAvailabilityServlet;

  private static final LocalServiceTestHelper helper =
          new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private static ByteArrayOutputStream errorStream;

  @BeforeEach
  void injectDependencies() {
    MockitoAnnotations.openMocks(this);
  }

  @BeforeEach
  void redirectErrorStream() {
    errorStream = new ByteArrayOutputStream();
    System.setErr(new PrintStream(errorStream));
  }

  @AfterEach
  void resetErrorStream() {
    System.setErr(System.err);
  }

  @BeforeAll
  static void setUp() {
    helper.setUp();
  }

  @AfterAll
  static void tearDown() {
    helper.tearDown();
  }

  /**
   * Get any messages that were printed on the error stream
   *
   * @return A string containing all errors
   */
  private String getErrors() {
    return errorStream.toString().trim();
  }

  /**
   * Sets up the user service mock to have a logged-in and valid user
   */
  private void setupUser() {
    when(userService.isUserLoggedIn()).thenReturn(true);
    when(userService.getCurrentUser()).thenReturn(user);
    when(user.getUserId()).thenReturn("anuserid");
    when(registrationServlet.isUserRegistered()).thenReturn(true);
    when(registrationServlet.isUserVolunteer()).thenReturn(true);
  }

  /** Sets the HTTP request parameters. Can also be used to pass null values. */
  private void setRequestParameters(String startTime, String endTime) {
    Map<String, String[]> parameterMap = new HashMap<>();
    parameterMap.put("ISO-start-time", new String[]{startTime});
    parameterMap.put("ISO-end-time", new String[]{endTime});
    when(request.getParameterMap()).thenReturn(parameterMap);
  }

  /**
   * Sets HTTP request parameters, but only if each value is not null
   */
  private void setNonNullRequestParameters(
          String firstname, String lastname, String type, String latitude, String longitude) {
    Map<String, String[]> parameterMap = new HashMap<>();
    if (firstname != null) parameterMap.put("firstname", new String[]{firstname});
    if (lastname != null) parameterMap.put("lastname", new String[]{lastname});
    if (type != null) parameterMap.put("type", new String[]{type});
    if (latitude != null) parameterMap.put("latitude", new String[]{latitude});
    if (longitude != null) parameterMap.put("longitude", new String[]{longitude});
    when(request.getParameterMap()).thenReturn(parameterMap);
  }

  /**
   * Calls the POST request, and asserts that the HTTP response was a certain value
   *
   * @param code The HTTP response code that the response contains
   */
  private void doPostAndAssertResponseCode(int code) {
    try {
      volunteerAvailabilityServlet.doPost(request, response);
      verify(response).sendError(code);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Calls the POST request, and asserts that the HTTP response and error message were a certain
   * value
   *
   * @param code The HTTP response code that the response contains
   */
  private void doPostAndAssertResponseCode(int code, String errorMessage) {
    try {
      volunteerAvailabilityServlet.doPost(request, response);
      verify(response).sendError(code, errorMessage);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Asserts whether a certain parameter was null in the HTTP request
   *
   * @param parameterName The name of the parameter, as passed in the HTTP request
   */
  private void checkNullParameter(String parameterName) {
    doPostAndAssertResponseCode(HttpServletResponse.SC_BAD_REQUEST, "");
  }

  /**
   * Asserts whether a certain parameter was null in the HTTP request
   *
   * @param parameterName The name of the parameter, as passed in the HTTP request
   */
  private void checkMissingParameter(String parameterName) {
    doPostAndAssertResponseCode(HttpServletResponse.SC_BAD_REQUEST, "TODO");
  }

  /**
   * Asserts that a parameter is blank in a request
   *
   * @param parameterName The name of the parameter that is blank
   */
  private void checkBlankParameter(String parameterName) {
    doPostAndAssertResponseCode(HttpServletResponse.SC_BAD_REQUEST, "TODO");
  }

  @Test
  void testPostUserNotLoggedInNotRegistered() throws Exception {
    when(userService.isUserLoggedIn()).thenReturn(false);

    when(registrationServlet.isUserRegistered()).thenReturn(false);
    when(registrationServlet.isUserIsolate()).thenReturn(false);
    when(registrationServlet.isUserVolunteer()).thenReturn(false);
    doPostAndAssertResponseCode(
            HttpServletResponse.SC_UNAUTHORIZED, "user must be logged in to post a request");
  }

  @Test
  void testPostUserLoggedInRegisteredAsIsolate() {
    when(userService.isUserLoggedIn()).thenReturn(true);

    when(registrationServlet.isUserRegistered()).thenReturn(true);
    when(registrationServlet.isUserIsolate()).thenReturn(true);
    when(registrationServlet.isUserVolunteer()).thenReturn(false);

    doPostAndAssertResponseCode(
            HttpServletResponse.SC_UNAUTHORIZED,
            "user must be registered as a volunteer to submit availability");
  }

  @Test
  public void testPostNullRequest() {
    volunteerAvailabilityServlet = new VolunteerAvailabilityServlet();
    assertThrows(
            IllegalArgumentException.class, () -> volunteerAvailabilityServlet.doPost(null, response));
  }

  @Test
  public void testPostNullResponse() {
    volunteerAvailabilityServlet = new VolunteerAvailabilityServlet();
    assertThrows(
            IllegalArgumentException.class, () -> volunteerAvailabilityServlet.doPost(request, null));
  }

  @Test
  public void testPostNotLoggedIn() {
    when(userService.isUserLoggedIn()).thenReturn(false);
    doPostAndAssertResponseCode(
            HttpServletResponse.SC_UNAUTHORIZED, "user must be logged in to post a request");
  }

  @Test
  public void testPostNoUserId() {
    when(userService.isUserLoggedIn()).thenReturn(true);
    when(userService.getCurrentUser()).thenReturn(user);
    doPostAndAssertResponseCode(HttpServletResponse.SC_UNAUTHORIZED, "user must be registered to submit availability");
  }

  @Test
  public void testPostEmptyRequest() {
    setupUser();
    doPostAndAssertResponseCode(HttpServletResponse.SC_BAD_REQUEST, "no parameters provided!");
  }
}
