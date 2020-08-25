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
import com.google.gson.Gson;
import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns the login status of the user. */
@WebServlet("/login-status")
public class LoginStatusServlet extends HttpServlet {
  @Inject private UserService userService;

  /**
   * Construct a LoginStatusServlet that depends on the default implementaion of UserService.
   */
  public LoginStatusServlet() {
    this.userService = UserServiceFactory.getUserService();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (response == null) {
      throw new IllegalArgumentException("response cannot be null");
    }

    if (request == null) {
      throw new IllegalArgumentException("request cannot be null");
    }

    boolean loggedIn = this.userService.isUserLoggedIn();

    Gson gson = new Gson();

    response.getWriter().println(gson.toJson(loggedIn));
  }
}
