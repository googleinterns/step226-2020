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

function initialise() {
    updateTimezoneOffset();
    populateRows();
}

function populateRows() {
    //TODO fetch existing timeslots for this volunteer from backend
    getExistingTimeSlots().then(rows => rows.forEach(row => addRow(row)));

    // add an empty row at bottom of form
    addEmptyRow();

    //TODO add delete button to each row

    //TODO validate fields so they're not empty, and start time is before end time
}

async function getExistingTimeSlots() {
    const slots = await (await fetch('/volunteer-availability')).json();
    return slots.map(slot => slotToRow(slot));
}

function slotToRow(slot) {
    //parse start.seconds and end.seconds, getting the date and the day in the local timezone
    const startTime = new Date(slot.start.seconds * 1000);
    const endTime = new Date(slot.end.seconds * 1000);
    //const date = startTime.getFullYear() + "-" + startTime.getMonth() + "-" + startTime.getDate();
    const date = startTime.toISOString().replace(/[A-Za-z]+.*/, "");
    const start = startTime.getHours() + ":" + startTime.getMinutes();
    const end = endTime.getHours() + ":" + endTime.getMinutes();
    return constructNewRow(date, start, end);
}

function updateTimezoneOffset() {
    document.getElementById('timezoneOffset').value =
        new Date().toTimeString().split(' ')[1].replace('GMT', '');
}

function constructNewRow(dateValue, startTimeValue, endTimeValue) {
    const emptyRow = document.createElement("div");
    const date = document.createElement("input");
    date.type = "date";
    date.name = "date";
    console.log("Date value: ", dateValue);
    date.value = dateValue;

    const startTime = document.createElement("input");
    startTime.type = "time";
    startTime.name = "start-time";
    startTime.value = startTimeValue;

    const endTime = document.createElement("input");
    endTime.type = "time";
    endTime.name = "end-time";
    endTime.value = endTimeValue;

    emptyRow.appendChild(date);
    emptyRow.appendChild(startTime);
    emptyRow.appendChild(endTime);

    console.log("New row", emptyRow);

    return emptyRow;
}

function addRow(row) {
    document.getElementById("availability-form").appendChild(row);
}

function addEmptyRow() {
    addRow(constructNewRow());
}