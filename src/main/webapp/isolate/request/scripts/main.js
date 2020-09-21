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
 * main.js is used to load necessary functions for the request.jsp page into
 * the global namespace. This is done so that all code can be written as ES
 * modules, allowing the global namespace to contain only the minimum number of
 * necessary members.
 */

import {addTicket, deleteTicket} from './tickets.js';
import {invertVisibility} from './input.js';

/* Add necessary functions to the global namespace. */

/**
 * Add a ticket to the request form.
 */
window.addTicket = async () => {
  try{
    addTicket();
  } catch (error) {
    alert('Could not add ticket to the request form. Please try again.');
  }
}

/**
 * Delete a ticket from the request form.
 */
window.deleteTicket = async (element) => {
  try{
    deleteTicket(element);
  } catch (error) {
    alert('Could not delete ticket from the request form. Please try again.')
  }
};

/**
 * Invert the visibility of the delivery time input elements of the request form.
 */
window.invertTimeInputVisibility = () => {
  const START_OF_DAY = "00:00";
  const END_OF_DAY = "23:59";

  const startTimeContainer = document.getElementById('startTimeContainer');
  const endTimeContainer = document.getElementById('endTimeContainer');

  invertVisibility(startTimeContainer);
  invertVisibility(endTimeContainer);

  /* Update the values of the now hidden time input elements, to ensure that
   * the entire day is marked as available.
   */

  const startTime = document.getElementById('startTime');
  const endTime = document.getElementById('endTime');

  startTime.value = START_OF_DAY;
  endTime.value = END_OF_DAY;
};

window.onload = () => {
  const timezoneId = new Intl.DateTimeFormat().resolvedOptions().timeZone;
  const timezoneIdElement = document.getElementById('timezoneId');
  timezoneIdElement.value = timezoneId;
};