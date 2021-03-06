/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

window.onload = async () => {
  await populateTable();
}

const populateTable = async () => {
  const table = document.getElementById('requests');

  const res = await fetch('/fetch-requests');

  if (res.status === HTTP_STATUS_CODES.UNAUTHORIZED) {
    alert(
        'We were not able to fetch your requests as you are not logged in, or have not registered');
  } else if (res.status === HTTP_STATUS_CODES.INTERNAL_SERVER_ERROR) {
    alert(
        'We were not able to fetch your requests as there was an unexpected server error. Please try again.');
  }

  const requests = await res.json();

  /**
   * Create a ticket, with subject and details.
   * @param {string} subjectText The subject of the ticket
   * @param {string} detailsText The details of the ticket
   */
  const createTicket =
      (subjectText, detailsText) => {
        const ticket = document.createElement('div');
        const ticketBody = document.createElement('div');
        const subject = document.createElement('p');
        const details = document.createElement('h3');


        ticket.classList.add('card');
        ticketBody.classList.add('card-body');
        subject.classList.add('card-title');
        details.classList.add('card-text');

        ticket.appendChild(ticketBody);
        ticketBody.appendChild(subject);
        ticketBody.appendChild(details);

        subject.innerText = subjectText;
        details.innerText = detailsText;

        return ticket;
      };

  /**
   * Create a row of the requests table, with date, start time, and end time.
   * @param {string} dateText The date of the request
   * @param {string} startText The start time of the request's delivery window - must be in ISO8601 format
   * @param {string} endText The end time of the request's delivery window - must be in ISO8601 format
   */
  const createRow =
      (dateText, startText, endText) => {
        const row = document.createElement('tr');

        const date = document.createElement('td');
        const start = document.createElement('td');
        const end = document.createElement('td');

        date.innerText = dateText;
        start.innerText = moment.utc(startText).local().format('hh:mmA');
        end.innerText = moment.utc(endText).local().format('hh:mmA');

        row.appendChild(date);
        row.appendChild(start);
        row.appendChild(end);

        return row;
      };

  for (let request of requests) {
    const row = createRow(request.date, request.start, request.end);
    const tickets = document.createElement('td');

    /* Iterate through the tickets for a request, and add them to this row. */
    for (let i = 0; i < request.subjects.length; i++) {
      const ticket = createTicket(request.subjects[i], request.details[i]);
      tickets.appendChild(ticket);
    }

    row.appendChild(tickets);

    table.appendChild(row);
  }
};
