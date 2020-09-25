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
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MatchFetcherServletTest {
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
  MatchFetcherServlet matchFetcherServlet;

  private static final LocalServiceTestHelper helper =
          new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @BeforeEach
  void injectDependencies() {
    MockitoAnnotations.openMocks(this);
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
   * Sets up the user service mock to have a logged-in and valid user
   */
  private void setupUser() {
    when(userService.isUserLoggedIn()).thenReturn(true);
    when(userService.getCurrentUser()).thenReturn(user);
    when(user.getUserId()).thenReturn("anuserid");
    when(registrationServlet.isUserRegistered()).thenReturn(true);
    when(registrationServlet.isUserVolunteer()).thenReturn(true);
  }

  @Test
  public void testGetNullRequest() {
    assertThrows(IllegalArgumentException.class, () -> matchFetcherServlet.doGet(null, response));
  }

  @Test
  public void testGetNullResponse() {
    assertThrows(IllegalArgumentException.class, () -> matchFetcherServlet.doGet(request, null));
  }

  @Test
  void testGetUserLoggedInNotRegistered() throws IOException {
    when(userService.isUserLoggedIn()).thenReturn(true);
    when(registrationServlet.isUserRegistered()).thenReturn(false);
    when(registrationServlet.isUserIsolate()).thenReturn(false);
    when(registrationServlet.isUserVolunteer()).thenReturn(false);

    matchFetcherServlet.doGet(request, response);
    verify(response)
            .sendError(
                    HttpServletResponse.SC_UNAUTHORIZED, "user must be registered to get matches");
  }

  @Test
  void testGetUserNotLoggedInNotRegistered() throws IOException {
    when(userService.isUserLoggedIn()).thenReturn(false);
    when(registrationServlet.isUserRegistered()).thenReturn(false);
    when(registrationServlet.isUserIsolate()).thenReturn(false);
    when(registrationServlet.isUserVolunteer()).thenReturn(false);

    matchFetcherServlet.doGet(request, response);
    verify(response)
            .sendError(
                    HttpServletResponse.SC_UNAUTHORIZED, "user must be logged in to get matches");
  }

  @Test
  public void testGetNullUser() throws IOException {
    setupUser();
    when(userService.getCurrentUser()).thenReturn(null);
    matchFetcherServlet.doGet(request, response);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "user is null!");
  }

  @Test
  public void testGetNullUserId() throws IOException {
    setupUser();
    User user = new User("email", "domain");
    when(userService.getCurrentUser()).thenReturn(user);
    matchFetcherServlet.doGet(request, response);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "userId is null!");
  }

  @Test
  public void testGetNormal() throws IOException {
    setupUser();
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    when(response.getWriter()).thenReturn(new PrintWriter(outputStream));
    matchFetcherServlet.doGet(request, response);
    assertNotNull(response);
    assert (outputStream.toString().isEmpty());
  }
}
