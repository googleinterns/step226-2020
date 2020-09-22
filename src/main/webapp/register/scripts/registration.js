const checkFormInputs =
    async () => {
  document.getElementById('registration-submit').disabled = true;

  const USER_TYPES = new Set(['isolate', 'volunteer']);

  const data = new Map([
    ['firstname', document.getElementById('firstname').value],
    ['lastname', document.getElementById('lastname').value],
    ['type', document.getElementById('type').value],
    ['address-textinput', document.getElementById('address-textinput').value],
    ['latitude', document.getElementById('latitude').value],
    ['longitude', document.getElementById('longitude').value]
  ]);

  /* Ensure none of the required parameters are null. */
  for (let [key, value] of data) {
    /* Loose equality is used to catch undefined values. */
    /* TODO: An address is not mandatory as of the current version of the api,
     * this is expected to change. */
    if (key !== 'address-textinput' && value == null) {
      formFailure(`The ${key} input is missing. Please refresh the page and try again.'`);
      return;
    }
  }

  /* Ensure none of the required parameters are blank. */
  for (let [key, value] of data) {
    data.set(key, value.trim());

    /* TODO: An address is not mandatory as of the current version of the api,
     * this is expected to change. */
    if (key !== 'address-textinput' && data.get(key) === '') {
      formFailure(`The ${key} input is empty. Please fill it out and try again.`);
      return;
    }
  }

  if (data.get('firstname').length < 2) {
    formFailure(`First Name is too short. The minimum allowed length is 2.`);
    return;
  }

  if (data.get('firstname').length > 300) {
    formFailure(`First Name is too long. The maximum allowed length is 300.`);
    return;
  }

  if (data.get('lastname').length < 2) {
    formFailure(`Last Name is too short. The minimum allowed length is 2.`);
    return;
  }

  if (data.get('lastname').length > 300) {
    formFailure(`Last Name is too long. The maximum allowed length is 300.`);
    return;
  }

  if (!USER_TYPES.has(data.get('type').toLowerCase())) {
    formFailure(`The selected reason for registration was not recognised. Please try again.`);
    return;
  }

  /* In order to be compatible with the existing API, the data must be encoded
   * as x-www-form-urlencoded. This is the default encoding used when a HTML
   * form is submitted. For more information about this encoding, see:
   * https://url.spec.whatwg.org/#application/x-www-form-urlencoded */
  const formEncodedData = [];
  data.forEach((value, key) => {
    formEncodedData.push(`${key}=${value}`);
  });

  const body = formEncodedData.join('&');

  const response = await fetch('/registration', {
    method: 'POST',
    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    body: body
  });

  const HTTP_STATUS_CODES = {
    INTERNAL_SERVER_ERROR: 500,
    BAD_REQUEST : 400,
    UNAUTHORIZED: 401
  };

  /* Display the relevant modal depending on the server response. */
  if (response.ok) {
    $('#successModal').modal(null);
    setTimeout(() => window.location.href = `/${data.get('type')}/home.html`, 5000);
  } else if (response.status === HTTP_STATUS_CODES.UNAUTHORIZED) {
    attachLoginLink();
    formFailure(`You must log in before registering.`);
  } else if (response.status === HTTP_STATUS_CODES.BAD_REQUEST) {
    formFailure('The server identified a problem with one or more of the form inputs. Please try again.');
  } else if (response.status === HTTP_STATUS_CODES.INTERNAL_SERVER_ERROR) {
    formFailure('The server experienced an unexpected error. Please try again.');
  } else {
    formFailure('An unexpected error occurred. Please try again.');
  }
}

const formFailure = (errorMessage) => {
  const modalText = document.getElementById('failureModalText');
  modalText.innerText = errorMessage;
  $('#failureModal').modal(null);
  document.getElementById('registration-submit').disabled = false;
}

const attachLoginLink = () => {
  const footer = document.getElementById('failureModalFooter');
  const loginLink = document.createElement('a');

  footer.innerHTML = '';

  loginLink.classList.add('btn', 'btn-primary');

  loginLink.href = '/login?redirectURL=/register/';
  loginLink.innerText = 'Login';

  footer.appendChild(loginLink);
}