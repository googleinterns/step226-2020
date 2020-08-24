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

import com.google.appengine.api.users.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoginStatusServletTest {
  @Mock UserService userService;

  @Mock HttpServletRequest request;

  @Mock HttpServletResponse response;

  /* The @InjectMocks annotation instructs Mockito to inject the mocked
   * UserService as a dependency of the LoginStatusServlet. */
  @InjectMocks LoginStatusServlet loginStatusServlet;

  @BeforeEach
  void injectDependencies() {
    /* Initialise all of the Mocks declared above. */
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Assert that an IllegalArgumentException is thrown when the request parameter is null.
   */
  @Test
  public void testGetNullRequest() throws IOException {
    System.out.println("===LOGINSTATUS SERVLET=== " + loginStatusServlet);
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
      loginStatusServlet.doGet(null, response);
    });

    assertEquals("request cannot be null", exception.getMessage());
  }

  /**
   * Assert that an IllegalArgumentException is thrown when the response parameter is null.
   */
  @Test
  public void testGetNullResponse() throws IOException {
    System.out.println("===LOGINSTATUS SERVLET=== " + loginStatusServlet);
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
      loginStatusServlet.doGet(request, null);
    });

    assertEquals("response cannot be null", exception.getMessage());
  }

  /**
   * Assert that "false" is returned when a User is logged out.
   */
  @Test
  public void testGetUserLoggedOut() throws IOException {
    System.out.println("88: ===LOGINSTATUS SERVLET=== " + loginStatusServlet);
    when(userService.isUserLoggedIn()).thenReturn(false);

    System.out.println("88: ===LOGINSTATUS SERVLET=== " + loginStatusServlet);
    loginStatusServlet.doGet(request, response);

    verify(response).getWriter().println("false");
  }

  /**
   * Assert that "true" is returned when a User is logged in.
   */
  @Test
  public void testGetUserLoggedIn() throws IOException {
    System.out.println("102: ===LOGINSTATUS SERVLET=== " + loginStatusServlet);
    when(userService.isUserLoggedIn()).thenReturn(true);

    System.out.println("102: ===LOGINSTATUS SERVLET=== " + loginStatusServlet);
    loginStatusServlet.doGet(request, response);

    verify(response).getWriter().println("true");
  }
}
