window.runMatching = async () => {
  const statusElement = document.getElementById('status');

  statusElement.innerText = 'Awaiting response from server.';
  statusElement.classList.add('alert-warning');

  const res = await fetch('/admin/run-matching' , {method: 'POST'});

  statusElement.classList.remove('alert-warning');
  statusElement.innerText = '';

  if (res.ok) {
    statusElement.classList.add('alert-success');
    statusElement.innerText = 'Matching Success!';
  } else {
    statusElement.classList.add('alert-danger');
    statusElement.innerText = 'Matching Failure!';
  }
};
