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
import java.io.PrintWriter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

  @Test
  void testPostUserNotLoggedInNotRegistered() {
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
    assertThrows(
            IllegalArgumentException.class, () -> volunteerAvailabilityServlet.doPost(null, response));
  }

  @Test
  public void testPostNullResponse() {
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
    doPostAndAssertResponseCode(
            HttpServletResponse.SC_UNAUTHORIZED, "user must be registered to submit availability");
  }

  @Test
  public void testPostEmptyRequest() {
    setupUser();
    doPostAndAssertResponseCode(HttpServletResponse.SC_BAD_REQUEST, "no parameters provided!");
  }

  @Test
  public void testPostNormalRequest() {
    setupUser();
    Map<String, String[]> parameterMap = request.getParameterMap();
    parameterMap.put("ISO-start-time", new String[]{"2020-09-17T16:04:00.000Z"});
    parameterMap.put("ISO-end-time", new String[]{"2020-09-17T22:04:00.000Z"});
    when(request.getParameterMap()).thenReturn(parameterMap);

    try {
      volunteerAvailabilityServlet.doPost(request, response);
      verify(response).sendRedirect("volunteer/availability.jsp");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testPostNullUser() {
    setupUser();
    when(userService.getCurrentUser()).thenReturn(null);
    Map<String, String[]> parameterMap = request.getParameterMap();
    parameterMap.put("ISO-start-time", new String[]{"AAAA", "BB"});
    parameterMap.put("ISO-end-time", new String[]{"AAAA", "BB"});
    when(request.getParameterMap()).thenReturn(parameterMap);

    doPostAndAssertResponseCode(HttpServletResponse.SC_UNAUTHORIZED, "user is null!");
  }

  @Test
  public void testPostNullUserId() {
    setupUser();
    User user = new User("email", "domain");
    when(userService.getCurrentUser()).thenReturn(user);
    Map<String, String[]> parameterMap = request.getParameterMap();
    parameterMap.put("ISO-start-time", new String[]{"AAAA", "BB"});
    parameterMap.put("ISO-end-time", new String[]{"AAAA", "BB"});
    when(request.getParameterMap()).thenReturn(parameterMap);

    doPostAndAssertResponseCode(HttpServletResponse.SC_UNAUTHORIZED, "user id is null!");
  }

  @Test
  public void testPostNullStartTimes() {
    setupUser();
    Map<String, String[]> parameterMap = request.getParameterMap();
    parameterMap.put("ISO-start-time", null);
    parameterMap.put("ISO-end-time", new String[]{"AAAA", "BB"});
    when(request.getParameterMap()).thenReturn(parameterMap);

    doPostAndAssertResponseCode(HttpServletResponse.SC_BAD_REQUEST, "start times not provided!");
  }

  @Test
  public void testPostNullEndTimes() {
    setupUser();
    Map<String, String[]> parameterMap = request.getParameterMap();
    parameterMap.put("ISO-start-time", new String[]{"AAAA", "BB"});
    parameterMap.put("ISO-end-time", null);
    when(request.getParameterMap()).thenReturn(parameterMap);

    doPostAndAssertResponseCode(HttpServletResponse.SC_BAD_REQUEST, "end times not provided!");
  }

  @Test
  public void testPostInvalidStartTimes() throws IOException {
    setupUser();
    Map<String, String[]> parameterMap = request.getParameterMap();
    parameterMap.put("ISO-start-time", new String[]{"djihvfiuwehgv"});
    parameterMap.put("ISO-end-time", new String[]{"AAAA", "BB"});
    when(request.getParameterMap()).thenReturn(parameterMap);

    volunteerAvailabilityServlet.doPost(request, response);
    assertEquals("Error parsing volunteer timeslot times!", getErrors());
  }

  @Test
  public void testPostInvalidEndTimes() throws IOException {
    setupUser();
    Map<String, String[]> parameterMap = request.getParameterMap();
    parameterMap.put("ISO-start-time", new String[]{"AAAA", "BB"});
    parameterMap.put("ISO-end-time", new String[]{"werty"});
    when(request.getParameterMap()).thenReturn(parameterMap);

    volunteerAvailabilityServlet.doPost(request, response);
    assertEquals("Error parsing volunteer timeslot times!", getErrors());
  }

  @Test
  public void testPostNullTimes() throws IOException {
    setupUser();
    Map<String, String[]> parameterMap = request.getParameterMap();
    parameterMap.put("ISO-start-time", new String[]{null});
    parameterMap.put("ISO-end-time", new String[]{"swred"});
    when(request.getParameterMap()).thenReturn(parameterMap);

    volunteerAvailabilityServlet.doPost(request, response);
    assertEquals("Volunteer timeslot times are null!", getErrors());
  }

  @Test
  public void testPostDifferentLengthTimes() throws IOException {
    setupUser();
    Map<String, String[]> parameterMap = request.getParameterMap();
    parameterMap.put("ISO-start-time", new String[]{"AAAA", "BB"});
    parameterMap.put("ISO-end-time", new String[]{"werty"});
    when(request.getParameterMap()).thenReturn(parameterMap);

    // Expected not to through an exception.
    volunteerAvailabilityServlet.doPost(request, response);
  }

  ////////////////// GET REQUEST TESTS ////////////////////////////

  @Test
  public void testGetNullRequest() {
    assertThrows(
            IllegalArgumentException.class, () -> volunteerAvailabilityServlet.doGet(null, response));
  }

  @Test
  public void testGetNullResponse() {
    assertThrows(
            IllegalArgumentException.class, () -> volunteerAvailabilityServlet.doGet(request, null));
  }

  @Test
  void testGetUserLoggedInNotRegistered() throws IOException {
    when(userService.isUserLoggedIn()).thenReturn(true);
    when(registrationServlet.isUserRegistered()).thenReturn(false);
    when(registrationServlet.isUserIsolate()).thenReturn(false);
    when(registrationServlet.isUserVolunteer()).thenReturn(false);

    volunteerAvailabilityServlet.doGet(request, response);
    verify(response)
            .sendError(
                    HttpServletResponse.SC_UNAUTHORIZED, "user must be registered to get availability");
  }

  @Test
  void testGetUserNotLoggedInNotRegistered() throws IOException {
    when(userService.isUserLoggedIn()).thenReturn(false);
    when(registrationServlet.isUserRegistered()).thenReturn(false);
    when(registrationServlet.isUserIsolate()).thenReturn(false);
    when(registrationServlet.isUserVolunteer()).thenReturn(false);

    volunteerAvailabilityServlet.doGet(request, response);
    verify(response)
            .sendError(
                    HttpServletResponse.SC_UNAUTHORIZED, "user must be logged in to get availability");
  }

  @Test
  void testGetUserLoggedInRegisteredAsIsolate() throws IOException {
    when(userService.isUserLoggedIn()).thenReturn(true);
    when(registrationServlet.isUserRegistered()).thenReturn(true);
    when(registrationServlet.isUserIsolate()).thenReturn(true);
    when(registrationServlet.isUserVolunteer()).thenReturn(false);

    volunteerAvailabilityServlet.doGet(request, response);
    verify(response)
            .sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "user must be registered as a volunteer to get availability");
  }

  @Test
  public void testGetNullUser() throws IOException {
    setupUser();
    when(userService.getCurrentUser()).thenReturn(null);
    volunteerAvailabilityServlet.doGet(request, response);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "user is null!");
  }

  @Test
  public void testGetNullUserId() throws IOException {
    setupUser();
    User user = new User("email", "domain");
    when(userService.getCurrentUser()).thenReturn(user);
    volunteerAvailabilityServlet.doGet(request, response);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "userId is null!");
  }

  @Test
  public void testGetNormal() throws IOException {
    setupUser();
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    when(response.getWriter()).thenReturn(new PrintWriter(outputStream));
    volunteerAvailabilityServlet.doGet(request, response);
    assertNotNull(response);
    assert (outputStream.toString().isEmpty());
  }
}
