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

package com.google.sps.servlets;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegistrationServletTest {

  @Mock
  UserService userService;
  @Mock
  HttpServletRequest request;
  @Mock
  HttpServletResponse response;
  @Mock
  User user;
  @InjectMocks
  RegistrationServlet registrationServlet;

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
    return errorStream.toString();
  }

  @Test
  public void testPostNullRequest() {
    registrationServlet = new RegistrationServlet();
    HttpServletResponse response = mock(HttpServletResponse.class);
    registrationServlet.doPost(null, response);
  }

  @Test
  public void testPostNotLoggedIn() {
    when(userService.isUserLoggedIn()).thenReturn(false);

    ArgumentCaptor<Integer> valueCapture = ArgumentCaptor.forClass(Integer.class);
    doNothing().when(response).setStatus(valueCapture.capture());

    registrationServlet.doPost(request, response);
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, valueCapture.getValue());
  }

  @Test
  public void testPostNoUserId() {
    when(userService.isUserLoggedIn()).thenReturn(true);
    when(userService.getCurrentUser()).thenReturn(user);

    ArgumentCaptor<Integer> valueCapture = ArgumentCaptor.forClass(Integer.class);
    doNothing().when(response).setStatus(valueCapture.capture());

    registrationServlet.doPost(request, response);
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, valueCapture.getValue());
  }

  /**
   * Sets up the user service mock to have a logged-in and valid user
   */
  private void setupUser() {
    when(userService.isUserLoggedIn()).thenReturn(true);
    when(userService.getCurrentUser()).thenReturn(user);
    when(user.getUserId()).thenReturn("anuserid");
  }

  /**
   * Sets up an argument captor to get the HTTP status code from the response
   *
   * @return The argument captor that will contain the HTTp status code, as an Integer
   */
  private ArgumentCaptor<Integer> captureResponseStatus() {
    ArgumentCaptor<Integer> valueCapture = ArgumentCaptor.forClass(Integer.class);
    doNothing().when(response).setStatus(valueCapture.capture());
    return valueCapture;
  }

  /**
   * Sets the HTTP request parameters. Can also be used to pass null values.
   */
  private void setRequestParameters(
          String firstname, String lastname, String type, String latitude, String longitude) {
    Map<String, String[]> parameterMap = new HashMap<>();
    parameterMap.put("firstname", new String[]{firstname});
    parameterMap.put("lastname", new String[]{lastname});
    parameterMap.put("type", new String[]{type});
    parameterMap.put("latitude", new String[]{latitude});
    parameterMap.put("longitude", new String[]{longitude});
    when(request.getParameterMap()).thenReturn(parameterMap);
  }

  @Test
  public void testPostEmptyRequest() {
    setupUser();
    ArgumentCaptor<Integer> valueCapture = captureResponseStatus();
    registrationServlet.doPost(request, response);
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, valueCapture.getValue());
  }

  @Test
  public void testPostNullFirstname() {
    setupUser();
    setRequestParameters(null, "alastname", "volunteer", "-85.300738", "-85.300738");
    ArgumentCaptor<Integer> valueCapture = captureResponseStatus();
    registrationServlet.doPost(request, response);
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, valueCapture.getValue());
    assertEquals("Parameter firstname is null!", getErrors());
  }

  @Test
  public void testPostNullLastname() {
    setupUser();
    setRequestParameters("afirstname", null, "volunteer", "-79.013542", "-85.300738");
    ArgumentCaptor<Integer> valueCapture = captureResponseStatus();
    registrationServlet.doPost(request, response);
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, valueCapture.getValue());
    assertEquals("Parameter lastname is null!", getErrors());
  }

  @Test
  public void testPostNullType() {
    setupUser();
    setRequestParameters("afirstname", "alastname", null, "-85.300738", "-85.300738");
    ArgumentCaptor<Integer> valueCapture = captureResponseStatus();
    registrationServlet.doPost(request, response);
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, valueCapture.getValue());
    assertEquals("Parameter type is null!", getErrors());
  }

  @Test
  public void testPostNullLatitude() {
    setupUser();
    setRequestParameters("afirstname", "alastname", "volunteer", null, "-85.300738");
    ArgumentCaptor<Integer> valueCapture = captureResponseStatus();
    registrationServlet.doPost(request, response);
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, valueCapture.getValue());
    assertEquals("Parameter latitude is null!", getErrors());
  }

  @Test
  public void testPostNullLongitude() {
    setupUser();
    setRequestParameters("afirstname", "alastname", "volunteer", "-85.300738", null);
    ArgumentCaptor<Integer> valueCapture = captureResponseStatus();
    registrationServlet.doPost(request, response);
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, valueCapture.getValue());
    assertEquals("Parameter longitude is null!", getErrors());
  }

  // TODO test firstname, lastname, type, latitude, longitude as missing parameters
  // TODO test wrong input for type, latitude and longitude
  // TODO test length limits for firstname and lastname
  // TODO test size constraints for latitude & longitude (as doubles)
}
