// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.vinet.servlets;

import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.users.UserService;
import java.io.IOException;
import java.net.URL;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that returns a logout link, which when visited redirects the user to
 * the provided redirection URL.
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
  private UserService userService;

  /**
   * Construct a LogoutServlet that depends on the default implementaion of UserService.
   */
  public LogoutServlet() {
    this.userService = UserServiceFactory.getUserService();
  }

  /**
   * Construct a LogoutServlet that depends on the provided implementaion of UserService.
   */
  public LogoutServlet(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (response == null) {
      throw new IllegalArgumentException("response cannot be null");
    }

    if (request == null) {
      throw new IllegalArgumentException("request cannot be null");
    }

    final String redirectURL = request.getParameter("redirectURL");

    if(redirectURL == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "redirectURL parameter missing");
      // A null redirectURL is unrecoverable, so the servlet must quit after sending an error code.
      return;
    }

    try {
      final String logout = this.userService.createLogoutURL(redirectURL);
      response.sendRedirect(logout);
    }
    /* UserService will throw an IllegalArgumentException if redirectURL is
       invalid. */
    catch (IllegalArgumentException ex) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "redirectURL parameter invalid");
    }
  }
}
