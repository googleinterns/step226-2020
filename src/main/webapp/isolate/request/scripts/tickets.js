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

import {TicketFactory} from './TicketFactory.js';
import {removeFromDOM} from '/scripts/DOMUtils.js';


/* Create a TicketFactory to create tickets during the session */
const ticketFactory = new TicketFactory();

/**
 * Delete a ticket from the request submission form.
 * @param {!HTMLElement} ticket The ticket to be deleted from the form.
 * @throws {TypeError} Will throw a TypeError if ticket is not viable for
 *     deletion.
 */
export const deleteTicket =
    (ticket) => {
      const TYPE_MISMATCH_PREFIX = `Ticket to be removed from form must not be`;

      if (ticket === null) {
        throw (TypeError(`${TYPE_MISMATCH_PREFIX} null.`));
      }

      if (typeof ticket === 'undefined') {
        throw (TypeError(`${TYPE_MISMATCH_PREFIX} undefined.`));
      }

      try {
        removeFromDOM(ticket);
      } catch (error) {
        throw (Error(`Removal from DOM failed with error:\n${error}`));
      }
    }

/**
 * Add a request ticket to the request form.
 * @throws Will throw an Error if the server cannot retrieve the
 *     Ticket Template.
 */
export const addTicket =
    async () => {
  const ticketsContainer = document.getElementById('tickets');

  try{
    ticketsContainer.appendChild(await createRequestTicket());
  } catch(error) {
    throw(`Could not add ticket to the request form, failed with error: ${error}`);
  }
}


/**
 * Create a request ticket.
 * @return a new Ticket.
 * @throws Will throw an Error if the server cannot retrieve the
 *     Ticket Template.
 */
const createRequestTicket = async () => {
  try{
    const ticket = await ticketFactory.createTicket();
    return ticket;
  } catch (error) {
    throw(error);
  }
}
