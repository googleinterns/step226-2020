package com.google.vinet.servlets;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.Registration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class RequestServletTest {
  @Mock DatastoreService datastore;
  
  @Mock UserService userService;
  
  @Mock RegistrationServlet registrationServlet;

  @Mock HttpServletRequest request;

  @Mock HttpServletResponse response;

  @InjectMocks RequestServlet requestServlet;

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @BeforeEach
  public void setUp() {
    helper.setUp();
  }

  @AfterEach
  public void tearDown() {
    helper.tearDown();
  }

  @BeforeEach
  void injectDependencies() throws Exception {
    /* Initialise all of the Mocks declared above. */
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testPostNullRequest() throws Exception {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> requestServlet.doPost(null, response));

    assertEquals("request must not be null", exception.getMessage());
  }

  @Test
  void testPostNullResponse() throws Exception {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> requestServlet.doPost(request, null));

    assertEquals("response must not be null", exception.getMessage());
  }

  @Test
  void testPostUserNotLoggedInNotRegistered() throws Exception {
    when(userService.isUserLoggedIn()).thenReturn(false);

    when(registrationServlet.isUserRegistered()).thenReturn(false);
    when(registrationServlet.isUserIsolate()).thenReturn(false);
    when(registrationServlet.isUserVolunteer()).thenReturn(false);

    requestServlet.doPost(request, response);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "user must be logged in to post a request");
  }

  
  @Test
  void testPostUserLoggedInNotRegistered() throws Exception {
    when(userService.isUserLoggedIn()).thenReturn(true);

    when(registrationServlet.isUserRegistered()).thenReturn(false);
    when(registrationServlet.isUserIsolate()).thenReturn(false);
    when(registrationServlet.isUserVolunteer()).thenReturn(false);

    requestServlet.doPost(request, response);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "user must be registered to post a request");
  }

  @Test
  void testPostUserLoggedInRegisteredAsVolunteer() throws Exception {
    when(userService.isUserLoggedIn()).thenReturn(true);

    when(registrationServlet.isUserRegistered()).thenReturn(true);
    when(registrationServlet.isUserIsolate()).thenReturn(false);
    when(registrationServlet.isUserVolunteer()).thenReturn(true);

    requestServlet.doPost(request, response);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "user must be registered as an isolate to post a request");
  }

  @Test
  void testPostUserLoggedInRegisteredAsIsolate() throws Exception {
    when(userService.isUserLoggedIn()).thenReturn(true);

    when(registrationServlet.isUserRegistered()).thenReturn(true);
    when(registrationServlet.isUserIsolate()).thenReturn(true);
    when(registrationServlet.isUserVolunteer()).thenReturn(false);

    requestServlet.doPost(request, response);
    verify(response, never()).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
  }

  @Test
  void testPostTicketsInOrder() throws Exception {
    final String[] subjects = {"subject 1", "subject 2", "subject 3"};
    final String[] details = {"detail 1", "detail 2", "detail 3"};

    final User user = mock(User.class);
    when(user.getUserId()).thenReturn("example");

    when(request.getParameter("timezoneId")).thenReturn("Europe/Paris");
    when(request.getParameter("date")).thenReturn("2020-09-12");
    when(request.getParameter("duration")).thenReturn("30");
    when(request.getParameter("startTime")).thenReturn("12:00:00");
    when(request.getParameter("endTime")).thenReturn("13:00:00");

    when(request.getParameterValues("subject")).thenReturn(subjects);
    when(request.getParameterValues("details")).thenReturn(details);

    when(userService.isUserLoggedIn()).thenReturn(true);
    when(userService.getCurrentUser()).thenReturn(user);

    when(registrationServlet.isUserRegistered()).thenReturn(true);
    when(registrationServlet.isUserIsolate()).thenReturn(true);
    when(registrationServlet.isUserVolunteer()).thenReturn(false);

    requestServlet.doPost(request, response);

    Entity entity = mock(Entity.class);

    ArgumentCaptor<Entity> ticketCaptor = ArgumentCaptor.forClass(Entity.class);

    verify(datastore).put(ticketCaptor.capture());

    assertEquals("[\"subject 1\",\"subject 2\",\"subject 3\"]", ticketCaptor.getValue().getProperty("subjects"));
    assertEquals("[\"detail 1\",\"detail 2\",\"detail 3\"]", ticketCaptor.getValue().getProperty("details"));
  }
}