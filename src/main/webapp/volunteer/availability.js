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
    getExistingTimeSlots().then(rows => rows.forEach(row => addRow(row)), (e) => alert(e));
}

/**
 * Fetches the list of existing timeslots.
 * @returns {Promise<Array>} Array of timeslot objects.
 */
async function getExistingTimeSlots() {
    try {
        const result = await fetch('/volunteer-availability');
        if (result.status === 401) return Promise.reject(new Error('Please login to use this feature!'));
        if (!result) return Promise.reject(new Error('fail'));
        const slots = await result.json();
        const rows = await Promise.all(slots.map(slot => slotToRow(slot)));
        await sortTimeSlots(rows);
        return rows;
    } catch (e) {
        return Promise.reject(new Error('There was an error loading existing slots!\nPlease refresh the page to try again.'));
    }
}

/**
 * Sorts the rows of timeslots by start date & time.
 * @param rows The array of rows to sort.
 */
async function sortTimeSlots(rows) {
    rows.sort((a, b) => (new Date(a.children["ISO-start-time"].value)).getTime() >
    (new Date(b.children["ISO-start-time"].value)).getTime() ? 1 : -1);
}

/**
 * Convert slot element to row div for insertion into form.
 * @param slot The slot element to convert.
 * @returns {Promise<HTMLDivElement>} The row containing the slot data.
 */
async function slotToRow(slot) {
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

    // Also update end time, as it is dependant on the start time's date value.
    fillISOEndTime(inputElement.parentNode.children["ISO-end-time"]);
}

/**
 * Set value of hidden ISO end time element by parsing start date and end time.
 * @param {HTMLInputElement} inputElement The end time element.
 */
function fillISOEndTime(inputElement) {
    const children = inputElement.parentNode.children;
    const endValue = inputElement.value;
    const ISOStartTime = children["ISO-start-time"]

    if (ISOStartTime == null || !ISOStartTime) return;

    // Get the date from the start datetime object
    const date = new Date(ISOStartTime.value);

    if (endValue == null || !endValue || date == null) return;

    const [first, second] = endValue.split(':');

    date.setHours(Number(first));
    date.setMinutes(Number(second));
    children["ISO-end-time"].value = date.toISOString();
}

/**
 * Creates a new row element with the timeslots details.
 * @param {Number} startTimeValue The initial value for datetime-local start date and time, as UNIX timestamp.
 * @param {String} endTimeValue The initial value for local end time, as HH:MM.
 * @returns {Promise<HTMLDivElement>} The div containing the row.
 */
async function constructNewRow(startTimeValue, endTimeValue) {
    const newRow = document.createElement("div");

    const [startElement, ISOStartElement, endElement, ISOEndElement, deleteButton] =
        await Promise.all([getStartElement(startTimeValue), getISOStartElement(),
            getEndTime(endTimeValue), getISOEndElement(), getDeleteButton()]);

    newRow.appendChild(startElement);
    newRow.appendChild(ISOStartElement);
    newRow.appendChild(endElement);
    newRow.appendChild(ISOEndElement);
    newRow.appendChild(deleteButton);

    // Update hidden ISO time values
    fillISOStartTime(startElement);

    // Validate time
    newRow.addEventListener("change", event => validateTimeSlot(event));
    newRow.isValid = false; // Default to false as there's no values

    return newRow;
}

/**
 * Creates a delete button for the specified row.
 * @returns {Promise<HTMLButtonElement>} The delete button element.
 */
async function getDeleteButton() {
    const button = document.createElement("button");
    button.innerHTML = "Delete";

    button.addEventListener("click", (event) => {
        const parent = event.target.parentNode;
        parent.parentNode.removeChild(parent);
    });

    return button;
}

/**
 * Construct the start datetime-local element in a timeslot row.
 * @param {Number} startTimeValue The value for datetime-local start date and time.
 * @returns {Promise<HTMLInputElement>} The start datetime-local element.
 */
async function getStartElement(startTimeValue) {
    const startTime = document.createElement("input");
    startTime.type = "datetime-local";
    startTime.name = "start-time";
    startTime.valueAsNumber = startTimeValue;
    startTime.required = true;

    startTime.addEventListener("change", (e) => fillISOStartTime(e.target));

    return startTime;
}

/**
 * Creates a hidden ISO start instant element.
 * @returns {Promise<HTMLInputElement>} The hidden text-input element.
 */
async function getISOStartElement() {
    const ISOStartTime = document.createElement("input");
    ISOStartTime.name = "ISO-start-time";
    ISOStartTime.hidden = true;
    return ISOStartTime;
}

/**
 * Creates a local time input element for the end time.
 * @param {String} endTimeValue The initial local time input, as HH:MM.
 * @returns {Promise<HTMLInputElement>} The local time input element.
 */
async function getEndTime(endTimeValue) {
    const endTime = document.createElement("input");
    endTime.type = "time";
    endTime.name = "end-time";
    endTime.value = endTimeValue;
    endTime.required = true;

    endTime.addEventListener("change", (e) => fillISOEndTime(e.target));

    return endTime;
}

/**
 * Creates a hidden ISO end instant element.
 * @returns {Promise<HTMLInputElement>} The hidden text-input element.
 */
async function getISOEndElement() {
    const ISOEndTime = document.createElement("input");
    ISOEndTime.name = "ISO-end-time";
    ISOEndTime.hidden = true;
    return ISOEndTime;
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
async function addEmptyRow() {
    addRow(await constructNewRow());
}

/**
 * Validate times picked so that start is before end time.
 */
function validateTimeSlot(event) {
    const div = event.target.parentNode;
    const children = div.children;
    console.log(event);
    const startTime = new Date(children["ISO-start-time"].value);
    const endTime = new Date(children["ISO-end-time"].value);

    console.log("start", startTime, "end", endTime);
    div.isValid = startTime.getTime() < endTime.getTime();
    console.log("is valid? " + div.isValid);

    //const anyInvalid = Array.from(div.parentNode.children).some(d => !d.isValid);
    let anyInvalid = false;
    const divs = div.parentNode.getElementsByTagName("div");
    for (let i = 0; i < divs.length; i++) {
        if (!divs[i].isValid) anyInvalid = true;
    }
    console.log("any invalid?", anyInvalid);

    // Disable submit button if any of the rows is not valid
    document.getElementById("availability-submit").disabled = anyInvalid;
}