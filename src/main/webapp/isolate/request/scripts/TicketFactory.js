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
    /* If the ticketTemplate has been cached previously, return immediately. */
    if (this.ticketTemplate !== null &&
        typeof this.ticketTemplate !== 'undefined') {
      return true;
    }
    
    const response = await fetch('request-ticket.html');

    /* If the request fails, immediately report failure. */
    if (!response.ok) {
      return false;
    }
    
    const text = await response.text();
    
    if (text === null || typeof text === 'undefined') {
      return false;
    }
    
    this.ticketTemplate = text;
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
    let cached = false;
    for (let i = 0; i < FETCH_LIMIT; i++) {
      cached = await this.cacheTicketTemplate();
      
      if (cached) {
        break;
      }
    }
    
    if (!cached) {
      throw (Error(`Ticket creation failed.`));
    }

    const ticketContainer = document.createElement('fieldset');
    const ticket = document.createElement('div');
    ticketContainer.classList.add('card');
    ticketContainer.classList.add('form-group');
    ticket.classList.add('card-body');

    ticketContainer.appendChild(ticket);

    ticket.innerHTML = this.ticketTemplate;

    this.ticketCount++;
    return ticketContainer;
  }
}
