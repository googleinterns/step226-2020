package com.google.vinet.servlets;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.*;
import com.google.gson.*;
import com.google.vinet.data.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

@WebServlet("/fetch-requests")
public class IsolateRequestServlet  extends HttpServlet{
  public DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  public UserService userService = UserServiceFactory.getUserService();
  public RegistrationServlet registrationServlet = new RegistrationServlet();

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

    final boolean registered = registrationServlet.isUserRegistered(this.userService);

    if (!registered) {
      response.sendError(
          HttpServletResponse.SC_UNAUTHORIZED, "user must be registered to fetch a request");
      return;
    }

    final String userId = userService.getCurrentUser().getUserId();

    final Query query = new Query(IsolateTimeSlot.ISOLATE_TIME_SLOT_TABLE_NAME);
    final Query.Filter filter = new Query.FilterPredicate("isolateId", Query.FilterOperator.EQUAL, userId);
    query.setFilter(filter).addSort("date", Query.SortDirection.DESCENDING);

    final PreparedQuery results = datastore.prepare(query);

    List<Match> isolateRequests = new LinkedList<>();

    Gson gson = new Gson();

    try{
      for (Entity entity : results.asIterable()){
        final String date = (String) entity.getProperty("date");
        final String start = OffsetDateTime.parse((String) entity.getProperty("startTime")).format(DateTimeFormatter.ISO_LOCAL_TIME);
        final String end = OffsetDateTime.parse((String) entity.getProperty("endTime")).format(DateTimeFormatter.ISO_LOCAL_TIME);

        final Key ticketKey = KeyFactory.stringToKey((String) entity.getProperty("ticketKey"));
        final Entity ticket = datastore.get(ticketKey);

        final String[] subjects = gson.fromJson((String) ticket.getProperty("subjects"), String[].class);
        final String[] details = gson.fromJson((String) ticket.getProperty("details"), String[].class);

        final Match isolateRequest = new Match(date, start, end, "", "", subjects, details);
        isolateRequests.add(isolateRequest);
      }
    } catch (EntityNotFoundException exception) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      /* Since an EntityNotFoundException implies the system has stored an isolate's request, but failed to link
       * that request to the correct Ticket entity in Datastore, the exception must be thrown to allow Google
       * Cloud Console to observe and log it. It should not be caught by the caller. */
      throw new RuntimeException(exception);
    }

    try {
      response.getWriter().println(gson.toJson(isolateRequests.toArray(new Match[] {})));
    } catch (Exception exception) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
