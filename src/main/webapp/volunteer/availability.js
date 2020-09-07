/*
 *  Copyright 2020 Google LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https:www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * Fills the webpage with the existing timeslots etc.
 */
function initialise() {
    populateRows();
}

/**
 * Inserts existing timeslots in webpage.
 */
function populateRows() {
    getExistingTimeSlots().then(rows => rows.forEach(row => addRow(row)));

    //TODO add delete button to each row

    //TODO validate fields so they're not empty, and start time is before end time
}

/**
 * Fetches the list of existing timeslots.
 * @returns {Promise<Array>} Array of timeslot objects.
 */
async function getExistingTimeSlots() {
    const slots = await (await fetch('/volunteer-availability')).json();
    const rows = slots.map(slot => slotToRow(slot));

    // Sort timeslots by start time
    return [...rows].sort((a, b) => (new Date(a.children["ISO-start-time"].value)).getTime() >
    (new Date(b.children["ISO-start-time"].value)).getTime() ? 1 : -1);
}

/**
 * Convert slot element to row div for insertion intro form.
 * @param slot The slot element to convert.
 * @returns {HTMLDivElement} The row containing the slot data.
 */
function slotToRow(slot) {
    // Get start and end instant and display as local timezone.
    const startTime = new Date(slot.start.seconds * 1000);
    const endTime = new Date(slot.end.seconds * 1000);

    // Get local time in HH:MM format.
    const hours = String(endTime.getHours()).padStart(2, "0");
    const minutes = String(endTime.getMinutes()).padStart(2, "0");
    const end = hours + ":" + minutes;

    return constructNewRow(startTime.getTime(), end);
}

/**
 * Set value of hidden ISO start time element by parsing start date and time.
 * @param {HTMLInputElement} inputElement The start datetime element.
 */
function fillISOStartTime(inputElement) {
    if (inputElement.value == null || inputElement.value === "") return;
    inputElement.parentNode.children["ISO-start-time"].value = new Date(inputElement.value).toISOString();
    //TODO also update end time accordingly to date change
    //TODO re-sort this slot in list after changing?
}

/**
 * Set value of hidden ISO end time element by parsing start date and end time.
 * @param {HTMLInputElement} inputElement The end time element.
 */
function fillISOEndTime(inputElement) {
    const children = inputElement.parentNode.children;
    const endValue = inputElement.value;

    // Get the date from the start datetime object
    const date = new Date(children["ISO-start-time"].value);

    if (endValue == null || !endValue || date == null) return;

    const timeParts = endValue.split(':');

    date.setHours(timeParts[0]);
    date.setMinutes(timeParts[1]);
    children["ISO-end-time"].value = date.toISOString();
}

/**
 * Creates a new row element with the timeslots details.
 * @param {String} startTimeValue The value for datetime-local start date and time.
 * @param {String} endTimeValue The value for local end time.
 * @returns {HTMLDivElement} The div containing the row.
 */
function constructNewRow(startTimeValue, endTimeValue) {
    const newRow = document.createElement("div");

    const startTime = document.createElement("input");
    startTime.type = "datetime-local";
    startTime.name = "start-time";
    startTime.valueAsNumber = startTimeValue;
    startTime.required = true;

    const ISOStartTime = document.createElement("input");
    ISOStartTime.name = "ISO-start-time";
    ISOStartTime.hidden = true;

    startTime.addEventListener("change", (e) => fillISOStartTime(e.target));

    const endTime = document.createElement("input");
    endTime.type = "time";
    endTime.name = "end-time";
    endTime.value = endTimeValue;
    endTime.required = true;

    const ISOEndTime = document.createElement("input");
    ISOEndTime.name = "ISO-end-time";
    ISOEndTime.hidden = true;

    endTime.addEventListener("change", (e) => fillISOEndTime(e.target));

    newRow.appendChild(startTime);
    newRow.appendChild(endTime);
    newRow.appendChild(ISOStartTime);
    newRow.appendChild(ISOEndTime);

    // Initialise with fetches values, if any
    fillISOStartTime(startTime);
    fillISOEndTime(endTime);
    return newRow;
}

/**
 * Add a row to the submission form.
 * @param {HTMLDivElement} row The row to add.
 */
function addRow(row) {
    document.getElementById("availability-form").appendChild(row);
}

/**
 * Insert an empty row in the submission form.
 */
function addEmptyRow() {
    addRow(constructNewRow());
}