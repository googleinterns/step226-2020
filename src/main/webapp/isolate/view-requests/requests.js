window.onload = async () => {
  const table = document.getElementById('requests');

  const res = await fetch('/fetch-requests');
  const requests = await res.json();

  console.log(requests);

  for(let request of requests) {
  const row = document.createElement('tr');

  const date = document.createElement('td');
  const start = document.createElement('td');
  const end = document.createElement('td');

  const tickets = document.createElement('td');

  date.innerText = request.date;
  start.innerText = request.start;
  end.innerText = request.end;

  /* Iterate through the tickets for a request, and add them to the table. */
  for (let i = 0; i < request.subjects.length; i++) {
    const ticket = document.createElement('div');
    const ticketBody = document.createElement('div');
    const subject = document.createElement('p');
    const detail = document.createElement('h3');


    ticket.classList.add('card');
    ticketBody.classList.add('card-body');
    subject.classList.add('card-title');
    detail.classList.add('card-text');

    ticket.appendChild(ticketBody);
    ticketBody.appendChild(subject);
    ticketBody.appendChild(detail);

    detail.innerText = request.details[i];
    subject.innerText = request.subjects[i];

    tickets.appendChild(ticket);
  }

  row.appendChild(date);
  row.appendChild(start);
  row.appendChild(end);
  row.appendChild(tickets);

  table.appendChild(row);
  }
}