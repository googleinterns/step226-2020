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

/* Create a TicketFactory to create tickets during the session */
const ticketFactory = new TicketFactory();

/**
 * Remove an element from the DOM.
 * @param {!HTMLElement} element The element to be removed from the DOM.
 * @throws {TypeError} Will throw a TypeError if element is not viable for
 *     removal from the DOM.
 */
const removeFromDOM = (element) => {
  const ERROR_PREFIX = `Element to be removed from DOM`;
  const TYPE_MISMATCH_PREFIX = `${ERROR_PREFIX} must not be`;
  const INVALID_PARENT_ELEMENT_MESSAGE =
      `${ERROR_PREFIX} has an invalid 'parentElement' attribute`;

  // Ensure that the element is not null.
  if (element === null) {
    throw (TypeError(`${TYPE_MISMATCH_PREFIX} null.`));
  }

  // Ensure that the element is not undefined.
  if (typeof element === 'undefined') {
    throw (TypeError(`${TYPE_MISMATCH_PREFIX} undefined.`));
  }

  let parent = null;

  // Ensure that the element has a parentElement attribute.
  try {
    parent = element.parentElement;
  } catch {
    throw (TypeError(`${ERROR_PREFIX} must have a 'parentElement' attribute`));
  }

  // Extract the parentElement attribute, only after ensuring it exists.
  parent = element.parentElement;

  // Ensure that the parent element is not null.
  if (parent === null) {
    throw (TypeError(INVALID_PARENT_ELEMENT_MESSAGE));
  }

  // Ensure that the parent element is not undefined.
  if (typeof parent === 'undefined') {
    throw (TypeError(INVALID_PARENT_ELEMENT_MESSAGE));
  }

  /*
   * Try to remove the element from the DOM.
   * If this fails, the parent element must not be a valid element on the DOM.
   */
  try {
    parent.removeChild(element);
  } catch {
    throw (TypeError(INVALID_PARENT_ELEMENT_MESSAGE));
  }
};

/**
 * Delete a ticket from the request submission form.
 * @param {!HTMLElement} ticket The ticket to be deleted from the form.
 * @throws {TypeError} Will throw a TypeError if ticket is not viable for
 *     deletion.
 */
window.deleteTicket =
    (ticket) => {
      const TYPE_MISMATCH_PREFIX = `Ticket to be removed from form must not be`;

      if (ticket === null) {
        throw (TypeError(`${TYPE_MISMATCH_PREFIX} null.`));
      }

      else if (typeof ticket === 'undefined') {
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
                 */
                window.addTicket =
        async () => {
      const form = document.getElementById('request-submission-form');
      form.appendChild(await createRequestTicket());
    }


/**
 * Create a request ticket.
 */
const createRequestTicket = async () => {
  const ticket = await ticketFactory.createTicket();
  return ticket;
}
