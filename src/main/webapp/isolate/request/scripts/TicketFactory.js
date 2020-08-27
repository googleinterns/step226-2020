/**
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Creates Tickets for embedding in the request form.
 * Tickets created by the TicketFactory have their name attributes assigned
 * programatically, to ensure that multiple tickets can be sent within a single
 * form.
 * 
 * This class __must__ be instantiated in order to function correctly.
 */
export class TicketFactory {
  /**
   * Construct a TicketFactory.
   */
  constructor() {
    /* The number of tickets created. */
    this.ticketCount = 0;
    /* The number of times the page has attempted to fetch the ticket template
     * from the server. */
    this.ticketTemplateFetchAttempts = 0;
    /* The template for request tickets. */
    this.ticketTemplate = null;
  }

  /**
   * Cache the Ticket Template, if necessary. If the Ticket Template already
   * exists, calling this function is a no-op.
   * 
   * @throws Will throw an Error if the server cannot retrieve the
   *     Ticket Template.
   */
  async cacheTicketTemplate() {
    /* cacheTicketTemplate is recursive. If ten or more failed attempts are made
     * to fetch the template, the process is aborted. */
    if (this.ticketTemplateFetchAttempts >= 10) {
      throw (Error('Ticket template cannot be fetched from the server.'));
    }

    /* If the ticketTemplate is null or undefined, attempt to fetch it from the
     * server. */
    if (this.ticketTemplate === null ||
        typeof this.ticketTemplate === 'undefined') {
      const response = await fetch('request-ticket.html');

      /* Each time the server is queried, increment the number of fetch attempts
       */
      this.ticketTemplateFetchAttempts++;

      /* If the request succeeds, attempt to parse the body of the response */
      /* If the request fails, try again. */
      if (response.ok) {
        const text = await response.text();

        /* If parsing the response body fails, try again. */
        if (text === null || typeof text === 'undefined') {
          this.cacheTicketTemplate();
        }
        /* If parsing the response body succeeds, cache the response body and return. */
        else {
          this.ticketTemplate = text;
          return;
        }
      } else {
        /* If the request has failed, try again. */
        this.cacheTicketTemplate();  
      }
    }
  }

  /**
   * Creates a Ticket.
   * @return a new Ticket
   * @throws Will throw an Error if the server cannot retrieve the
   *     Ticket Template.
   */
  async createTicket() {
    try {
      await this.cacheTicketTemplate();
    } catch (error) {
      throw (Error(`Ticket creation failed with error\n:${error}`));
    }

    const ticket = document.createElement('fieldset');
    ticket.innerHTML = this.ticketTemplate;

    /* Rename each ticket's input element */
    for (let child of ticket.children) {
      /*
       * The name of the tickets always resembles a 2D array. This ensures that
       * the browser can format the request properly, such that all of the
       * tickets are recieved on the backend within a single POST request.
       */
      child.name = `tickets[${this.ticketCount}][${child.name}]`;
    }

    this.ticketCount++;
    return ticket;
  }
}
