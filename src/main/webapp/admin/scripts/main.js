window.runMatching = async () => {
  const statusElement = document.getElementById('status');

  status.innerText = 'Awaiting response from server.';
  status.classList.add('alert-warning');

  const res = await fetch('/admin/run-matching');

  status.classList.remove('alert-warning');
  status.innerText = '';

  if (res.ok) {
    status.classList.add('alert-success');
    status.innerText = 'Matching Success!';
  } else {
    status.classList.add('alert-danger');
    status.innerText = 'Matching Failure!';
  }
};