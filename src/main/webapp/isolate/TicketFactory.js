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

export class TicketFactory {
  constructor() {
    /* ticketCount tracks the number of tickets created thus far */
    this.ticketCount = 0;
  }

  createTicket = async () => {
    // TODO: Cache template for each session
    const ticketTemplate = await (await fetch('request-ticket.html')).text();

    const ticket = document.createElement('fieldset');
    ticket.innerHTML = ticketTemplate;

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
