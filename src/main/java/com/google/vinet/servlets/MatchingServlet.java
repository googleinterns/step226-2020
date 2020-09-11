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

import com.google.vinet.data.MatchingRunner;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/run-matching")
public class MatchingServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
    /* Construct a MatchingRunner with no initial parameters. All necessary data will be
     * pulled from DataStore once runner.run() is called. */
    MatchingRunner runner = new MatchingRunner();

    /*
     * Try to run the matcher. Report any failures to the caller.
     * In a production environment, there would be an integration here with the bug tracking system
     * used by the deployer, to alert the owner that the matching has failed.
     * At present, all errors are visible in the Google Cloud Console, and email alerts can be set
     * up to emulate a paging system.
     */
    try {
      runner.run();
    } catch (Exception exception) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      throw exception;
    }
  }
}
