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
import static org.mockito.Mockito.verify;
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
    return errorStream.toString().trim();
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
   * @return The argument captor that will contain the HTTP status code, as an Integer
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
   * Calls the POST request, and asserts that the HTTP response to this request was a certain value
   *
   * @param code The HTTP response code that the response contains
   */
  private void assertResponseCode(int code) {
    registrationServlet.doPost(request, response);
    verify(response).setStatus(code);
  }

  /**
   * Asserts whether a certain parameter was null in the HTTP request
   *
   * @param parameterName The name of the parameter, as passed in the HTTP request
   */
  private void checkNullParameter(String parameterName) {
    assertResponseCode(HttpServletResponse.SC_BAD_REQUEST);
    assertEquals("Parameter " + parameterName + " is null!", getErrors());
  }

  /**
   * Asserts whether a certain parameter was null in the HTTP request
   *
   * @param parameterName The name of the parameter, as passed in the HTTP request
   */
  private void checkMissingParameter(String parameterName) {
    assertResponseCode(HttpServletResponse.SC_BAD_REQUEST);
    assertEquals("Missing parameter " + parameterName + " for registration request!", getErrors());
  }

  /**
   * Assert that a parameter is blank in a request
   *
   * @param parameterName The name of the parameter that is blank
   */
  private void checkBlankParameter(String parameterName) {
    assertResponseCode(HttpServletResponse.SC_BAD_REQUEST);
    assertEquals("Parameter " + parameterName + " is blank!", getErrors());
  }

  @Test
  public void testPostNullRequest() {
    registrationServlet = new RegistrationServlet();
    registrationServlet.doPost(null, response);
  }

  @Test
  public void testPostNotLoggedIn() {
    when(userService.isUserLoggedIn()).thenReturn(false);
    assertResponseCode(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  public void testPostNoUserId() {
    when(userService.isUserLoggedIn()).thenReturn(true);
    when(userService.getCurrentUser()).thenReturn(user);
    assertResponseCode(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  public void testPostEmptyRequest() {
    setupUser();
    assertResponseCode(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void testPostNullFirstname() {
    setupUser();
    setRequestParameters(null, "alastname", "volunteer", "-85.300738", "-85.300738");
    checkNullParameter("firstname");
  }

  @Test
  public void testPostNullLastname() {
    setupUser();
    setRequestParameters("afirstname", null, "volunteer", "-79.013542", "-85.300738");
    checkNullParameter("lastname");
  }

  @Test
  public void testPostNullType() {
    setupUser();
    setRequestParameters("afirstname", "alastname", null, "-85.300738", "-85.300738");
    checkNullParameter("type");
  }

  @Test
  public void testPostNullLatitude() {
    setupUser();
    setRequestParameters("afirstname", "alastname", "volunteer", null, "-85.300738");
    checkNullParameter("latitude");
  }

  @Test
  public void testPostNullLongitude() {
    setupUser();
    setRequestParameters("afirstname", "alastname", "volunteer", "-85.300738", null);
    checkNullParameter("longitude");
  }

  @Test
  public void testPostMissingFirstname() {
    setupUser();
    setNonNullRequestParameters(null, "alastname", "volunteer", "-85.300738", "-85.300738");
    checkMissingParameter("firstname");
  }

  @Test
  public void testPostMissingLastname() {
    setupUser();
    setNonNullRequestParameters("afirstname", null, "volunteer", "-85.300738", "-85.300738");
    checkMissingParameter("lastname");
  }

  @Test
  public void testPostMissingType() {
    setupUser();
    setNonNullRequestParameters("afirstname", "alastname", null, "-85.300738", "-85.300738");
    checkMissingParameter("type");
  }

  @Test
  public void testPostMissingLatitude() {
    setupUser();
    setNonNullRequestParameters("afirstname", "alastname", "volunteer", null, "-85.300738");
    checkMissingParameter("latitude");
  }

  @Test
  public void testPostMissingLongitude() {
    setupUser();
    setNonNullRequestParameters("afirstname", "alastname", "volunteer", "-85.300738", null);
    checkMissingParameter("longitude");
  }

  @Test
  public void testPostBlankFirstname() {
    setupUser();
    setNonNullRequestParameters("", "alastname", "volunteer", "-85.300738", "-85.300738");
    checkBlankParameter("firstname");
  }

  @Test
  public void testPostBlankLastname() {
    setupUser();
    setNonNullRequestParameters("afirstname", "       ", "volunteer", "-85.300738", "-85.300738");
    checkBlankParameter("lastname");
  }

  @Test
  public void testPostBlankType() {
    setupUser();
    setNonNullRequestParameters("afirstname", "alastname", "        ", "-85.300738", "-85.300738");
    checkBlankParameter("type");
  }

  @Test
  public void testPostBlankLatitude() {
    setupUser();
    setNonNullRequestParameters("afirstname", "alastname", "volunteer", "          ", "-85.300738");
    checkBlankParameter("latitude");
  }

  @Test
  public void testPostBlankLongitude() {
    setupUser();
    setNonNullRequestParameters(
            "afirstname", "alastname", "volunteer", "-85.300738", "                    ");
    checkBlankParameter("longitude");
  }

  @Test
  public void testPostInvalidType() {
    setupUser();
    setNonNullRequestParameters(
            "afirstname", "alastname", "ekugfghweig", "-85.300738", "-85.300738");
    assertResponseCode(HttpServletResponse.SC_BAD_REQUEST);
    assertEquals("Wrong type parameter!", getErrors());
  }

  @Test
  public void testPostInvalidLatitude() {
    setupUser();
    setNonNullRequestParameters("afirstname", "alastname", "isolate", "weifeuf", "-85.300738");
    assertResponseCode(HttpServletResponse.SC_BAD_REQUEST);
    assertEquals("The latitude is not formatted as a double!", getErrors());
  }

  @Test
  public void testPostInvalidLongitude() {
    setupUser();
    setNonNullRequestParameters("afirstname", "alastname", "isolate", "-85.300738", "newf873nf");
    assertResponseCode(HttpServletResponse.SC_BAD_REQUEST);
    assertEquals("The longitude is not formatted as a double!", getErrors());
  }

  @Test
  public void testPost1LetterFirstname() {
    setupUser();
    setNonNullRequestParameters("a", "alastname", "isolate", "-85.300738", "-85.300738");
    assertResponseCode(HttpServletResponse.SC_BAD_REQUEST);
    assertEquals("Parameter firstname is not within length bounds!", getErrors());
  }

  @Test
  public void testPost2LetterFirstname() {
    setupUser();
    setNonNullRequestParameters("ab", "alastname", "isolate", "-85.300738", "-85.300738");
    assertResponseCode(HttpServletResponse.SC_OK);
    assertEquals("", getErrors());
  }

  @Test
  public void testPostLongLastname() {
    setupUser();
    setNonNullRequestParameters(
            "abenthy",
            "alas tnamewbfjhukfjg orejg rg e3bfudrebjuhegujyddkuifheruigfh rui54rit y34795ty 87345 t785r4yh t845rht 875ryt 9875ry t5 54 yt54 y54 y654y ygffgh rth trh trh th t th thgrthrthrthrthtrhtrhtrhtrhrthrthtrhtrhthrthtrhtrhtrrrrrrrrrrrrrrrrrrevuyjeduyedgfjuyewgfruyefruyegfruye4g uye4wruywe4g uye4grfuywe4g rfuye4gfruye4w guyrf e4",
            "isolate",
            "-85.300738",
            "-85.300738");
    assertResponseCode(HttpServletResponse.SC_BAD_REQUEST);
    assertEquals("Parameter lastname is not within length bounds!", getErrors());
  }
}
