package com.google.vinet.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.Registration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
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

  @BeforeEach
  void injectDependencies() {
    /* Initialise all of the Mocks declared above. */
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Assert that an IllegalArgumentException is thrown when the request parameter is null.
   */
  @Test
  public void testGetNullRequest() throws Exception {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
      requestServlet.doGet(null, response);
    });

    assertEquals("request must not be null", exception.getMessage());
  }

  /**
   * Assert that an IllegalArgumentException is thrown when the response parameter is null.
   */
  @Test
  public void testGetNullResponse() throws Exception {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
      requestServlet.doGet(request, null);
    });

    assertEquals("response must not be null", exception.getMessage());
  }

  /**
   * Assert that a user who is logged out, but registered, cannot fetch a request.
   * (This should never happen, logging in a prerequisite for checking registration status.)
   */
  @Test
  public void testGetLoggedOutRegistered() throws Exception {
    when(userService.isUserLoggedIn()).thenReturn(false);
    when(registrationServlet.isUserRegistered(any())).thenReturn(true);
    
    requestServlet.doGet(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_UNAUTHORIZED,
        "user must be logged in to fetch a request"
    );

    verify(response, never()).sendError(
        HttpServletResponse.SC_UNAUTHORIZED,
        "user must be registered to fetch a request"
    );
  }

  /**
   * Assert that a user who is neither logged in NOR registered, cannot fetch a request.
   */
  @Test
  public void testGetLoggedOutNotRegistered() throws Exception {
    when(userService.isUserLoggedIn()).thenReturn(false);
    when(registrationServlet.isUserRegistered(any())).thenReturn(false);
    
    requestServlet.doGet(request, response);

    verify(response).sendError(
        HttpServletResponse.SC_UNAUTHORIZED,
        "user must be logged in to fetch a request"
    );

    verify(response, never()).sendError(
        HttpServletResponse.SC_UNAUTHORIZED,
        "user must be registered to fetch a request"
    );
  }

  /**
   * Assert that a user who is both logged in AND registered, cannot fetch a request.
   */
  @Test
  public void testGetLoggedInRegistered() throws Exception {
    when(userService.isUserLoggedIn()).thenReturn(true);
    when(registrationServlet.isUserRegistered(any())).thenReturn(true);

    requestServlet.doGet(request, response);

    verify(response, never()).sendError(
        HttpServletResponse.SC_UNAUTHORIZED,
        "user must be logged in to fetch a request"
    );

    verify(response, never()).sendError(
        HttpServletResponse.SC_UNAUTHORIZED,
        "user must be registered to fetch a request"
    );
  }

  /**
   * Assert that a user who is logged in, BUT NOT registered, cannot fetch a request.
   */
  @Test
  public void testGetLoggedInNotRegistered() throws Exception {
    when(userService.isUserLoggedIn()).thenReturn(true);
    when(registrationServlet.isUserRegistered(any())).thenReturn(false);

    requestServlet.doGet(request, response);

    verify(response, never()).sendError(
        HttpServletResponse.SC_UNAUTHORIZED,
        "user must be logged in to fetch a request"
    );

    verify(response).sendError(
        HttpServletResponse.SC_UNAUTHORIZED,
        "user must be registered to fetch a request"
    );
  }

  /**
   * Assert that, if there is no key parameter, a BAD_REQUEST (400) code is sent.
   */
  @Test
  public void testGetNullKey() throws Exception {
    when(userService.isUserLoggedIn()).thenReturn(true);
    when(registrationServlet.isUserRegistered(any())).thenReturn(true);

    when(request.getParameter("key")).thenReturn(null);

    requestServlet.doGet(request, response);

    verify(response).sendError(
      HttpServletResponse.SC_BAD_REQUEST
    );
  }

  /**
   * Assert that, if the key parameter is empty, a BAD_REQUEST (400) code is sent.
   */
  @Test
  public void testGetEmptyKey() throws Exception {
    when(userService.isUserLoggedIn()).thenReturn(true);
    when(registrationServlet.isUserRegistered(any())).thenReturn(true);

    when(request.getParameter("key")).thenReturn("    \t\t\t\n\n\n");

    requestServlet.doGet(request, response);

    verify(response).sendError(
      HttpServletResponse.SC_BAD_REQUEST
    );
  }

  /**
   * Assert that, if Datstore determines the key parameter is invalid, a BAD_REQUEST (400) code is
   * sent.
   */
  @Test
  public void testGetInvalidKey() throws Exception {
    when(userService.isUserLoggedIn()).thenReturn(true);
    when(registrationServlet.isUserRegistered(any())).thenReturn(true);

    when(request.getParameter("key")).thenReturn("key that is invalid");
    when(registrationServlet.stringToKey(anyString())).thenReturn(mock(Key.class));
    when(datastore.get(any(Key.class))).thenThrow(IllegalArgumentException.class);

    requestServlet.doGet(request, response);

    verify(response).sendError(
      HttpServletResponse.SC_BAD_REQUEST
    );
  }

  /**
   * Assert that, if Datstore cannot find an entity for the key parameter, a BAD_REQUEST (400) code
   * is sent.
   */
  @Test
  public void testGetKeyNotFound() throws Exception {
    when(userService.isUserLoggedIn()).thenReturn(true);
    when(registrationServlet.isUserRegistered(any())).thenReturn(true);

    when(request.getParameter("key")).thenReturn("key that can't be found");
    when(registrationServlet.stringToKey(anyString())).thenReturn(mock(Key.class));

    // Force datastore to not find an entity with the given key.
    when(datastore.get(any(Key.class))).thenThrow(EntityNotFoundException.class);

    requestServlet.doGet(request, response);

    verify(response).sendError(
      HttpServletResponse.SC_BAD_REQUEST
    );
  }


  /**
   * Assert that, if Datastore fails, an INTERNAL_SERVER_ERROR (500) code is sent.
   */
  @Test
  public void testGetDatastoreFailed() throws Exception {
    when(userService.isUserLoggedIn()).thenReturn(true);
    when(registrationServlet.isUserRegistered(any())).thenReturn(true);

    when(request.getParameter("key")).thenReturn("key that will cause datastore to fail");
    when(registrationServlet.stringToKey(anyString())).thenReturn(mock(Key.class));

    // Force datastore to fail.
    when(datastore.get(any(Key.class))).thenThrow(DatastoreFailureException.class);

    /* A DatstoreFailureException is thrown so that it can be intercepted, and the project owner
     * can be notified, as it represents a server-side error. */
    assertThrows(DatastoreFailureException.class, () -> requestServlet.doGet(request, response));

    verify(response).sendError(
      HttpServletResponse.SC_INTERNAL_SERVER_ERROR
    );
  }


  /**
   * Assert that an IllegalArgumentException is thrown when the request parameter is null.
   */
  @Test
  public void testPostNullRequest() throws Exception {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
      requestServlet.doPost(null, response);
    });

    assertEquals("request must not be null", exception.getMessage());
  }

  /**
   * Assert that an IllegalArgumentException is thrown when the response parameter is null.
   */
  @Test
  public void testPostNullResponse() throws Exception {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
      requestServlet.doPost(request, null);
    });

    assertEquals("response must not be null", exception.getMessage());
  }
}