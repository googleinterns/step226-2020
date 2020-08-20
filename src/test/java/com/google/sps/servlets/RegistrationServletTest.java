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

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

public class RegistrationServletTest {
  private static final LocalServiceTestHelper userServiceHelper =
          new LocalServiceTestHelper(new LocalUserServiceTestConfig());

  @BeforeAll
  static void setUp() {
    userServiceHelper.setUp();
  }

  @AfterAll
  static void tearDown() {
    userServiceHelper.tearDown();
  }

  @Test
  public void testPostNullRequest() {
    RegistrationServlet registrationServlet = new RegistrationServlet();
    HttpServletResponse response = mock(HttpServletResponse.class);
    registrationServlet.doPost(null, response);
  }

  @Test
  public void testPostNotLoggedIn() {
    userServiceHelper.setEnvIsLoggedIn(false);

    RegistrationServlet registrationServlet = new RegistrationServlet();
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    ArgumentCaptor<Integer> valueCapture = ArgumentCaptor.forClass(Integer.class);
    doNothing().when(response).setStatus(valueCapture.capture());

    registrationServlet.doPost(request, response);
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, valueCapture.getValue());
  }
}
