package com.google.vinet.servlets;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.*;
import com.google.gson.*;
import java.io.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

@WebServlet("/ticket")
public class TicketsServlet  extends HttpServlet{
  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private final UserService userService = UserServiceFactory.getUserService();
  private final RegistrationServlet registrationServlet = new RegistrationServlet();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (response == null) {
      throw new IllegalArgumentException("response must not be null");
    }

    if (request == null) {
      throw new IllegalArgumentException("request must not be null");
    }

    if (!this.userService.isUserLoggedIn()) {
      response.sendError(
          HttpServletResponse.SC_UNAUTHORIZED, "user must be logged in to fetch a request");
      return;
    }

    final boolean registered = registrationServlet.isUserRegistered();

    if (!registered) {
      response.sendError(
          HttpServletResponse.SC_UNAUTHORIZED, "user must be registered to fetch a request");
      return;
    }

    String id = request.getParameter("id");

    Key key = KeyFactory.createKey("Ticket", Long.parseLong(id));

    System.out.println(key);

    try {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      response.getWriter().println(gson.toJson(datastore.get(key).getProperties()));
    } catch (Exception exception) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}