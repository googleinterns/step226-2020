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
    /* The template for request tickets. */
    this.ticketTemplate = null;
  }

  /**
   * Attempt to cache the Ticket Template, if necessary. If the Ticket Template already
   * exists, calling this function is a no-op.
   * 
   * @return Whether the template has been cached.
   * @throws Will throw an Error if the server cannot retrieve the
   *     Ticket Template.
   */
  async cacheTicketTemplate() {
    /* If the ticketTemplate is null or undefined, attempt to fetch it from the
     * server. */
    if (this.ticketTemplate === null ||
        typeof this.ticketTemplate === 'undefined') {
      const response = await fetch('request-ticket.html');

      /* If the request succeeds, attempt to parse the body of the response */
      if (response.ok) {
        const text = await response.text();

        /* If parsing the response body fails, report failure. */
        if (text === null || typeof text === 'undefined') {
          return false;
        }
        /* If parsing the response body succeeds, cache the ticket template and report success. */
        else {
          this.ticketTemplate = text;
          return true;
        }
      }
      /* The response was not ok. Report failure. */
      return false;
    }
    /* The ticketTemplate has already been cached. Report success. */
    return true;
  }

  /**
   * Creates a Ticket.
   * @return a new Ticket
   * @throws Will throw an Error if the server cannot retrieve the
   *     Ticket Template.
   */
  async createTicket() {
    /* Attempt to cache the ticket template a maximum of 10 times. */ 
    const FETCH_LIMIT = 10;
    for (let i = 0; i < FETCH_LIMIT; i++) {
      const cached = await this.cacheTicketTemplate();
      
      if (cached) {
        break;
      }
    }
    
    if (!cached) {
      throw (Error(`Ticket creation failed.`));
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
