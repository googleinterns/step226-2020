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

public class LogoutServletTest {
  @Mock UserService userService;

  @Mock HttpServletRequest request;

  @Mock HttpServletResponse response;

  /* The @InjectMocks annotation instructs Mockito to inject the mocked
   * UserService as a dependency of the LogoutServlet. */
  @InjectMocks LogoutServlet logoutServlet;

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
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
      logoutServlet.doGet(null, response);
    });

    assertEquals("request cannot be null", exception.getMessage());
  }

  /**
   * Assert that an IllegalArgumentException is thrown when the response parameter is null.
   */
  @Test
  public void testGetNullResponse() throws IOException {
    when(request.getParameter("redirectURL")).
        thenReturn("/");

    Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
      logoutServlet.doGet(request, null);
    });

    assertEquals("response cannot be null", exception.getMessage());
  }

  /**
   * Assert that a BAD_REQUEST (503) HTTP error is sent when the redirectURL parameter is invalid.
   */
  @Test
  public void testGetInvalidRedirectURL() throws IOException {
    when(request.getParameter("redirectURL"))
        .thenReturn("");

    when(userService.createLogoutURL(anyString())).
        thenThrow(IllegalArgumentException.class);

    logoutServlet.doGet(request, response);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST,"redirectURL parameter invalid");
  }

  /**
   * Assert that a BAD_REQUEST (503) HTTP error is sent when the redirectURL parameter is omitted.
   */
  @Test
  public void testGetMissingRedirectURL() throws IOException {
    when(request.getParameter("redirectURL"))
        .thenReturn(null);

    logoutServlet.doGet(request, response);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "redirectURL parameter missing");
  }
}
