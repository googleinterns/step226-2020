window.onload = async () => {
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
        detail.innerText = detailsText;

        return ticket;
      };

  const createRow =
      (dateText, startText, endText) => {
        const row = document.createElement('tr');

        const date = document.createElement('td');
        const start = document.createElement('td');
        const end = document.createElement('td');

        date.innerText = dateText;
        start.innerText = startText;
        end.innerText = endText;

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
}