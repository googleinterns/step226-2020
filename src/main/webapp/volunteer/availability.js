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

function parseForm() {
    const form = document.getElementById("availability-form");
    const divs = form.getElementsByTagName("div");
    for (let div of divs) {
        const children = div.children;
        children["start-time"].value = children["start-time"].valueAsDate.toISOString();
        children["end-time"].value = children["end-time"].valueAsDate.toISOString();
    }
    console.log("Form contents", divs);
    // divs[1].children["date"].value
    return false; // prevent form submission
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
    const newRow = document.createElement("div");

    const startTime = document.createElement("input");
    startTime.type = "datetime-local";
    startTime.name = "start-time";
    startTime.value = startTimeValue;

    const ISOStartTime = document.createElement("input");
    ISOStartTime.name = "ISO-start-time";
    //ISOStartTime.hidden = true;

    startTime.addEventListener("change", (e) => {
        const inputElement = e.target;
        inputElement.parentNode.children["ISO-start-time"].value = new Date(inputElement.value).toISOString();
    });

    const endTime = document.createElement("input");
    endTime.type = "time";
    endTime.name = "end-time";
    endTime.value = endTimeValue;

    const ISOEndTime = document.createElement("input");
    ISOEndTime.name = "ISO-end-time";
    //ISOEndTime.hidden = true;

    endTime.addEventListener("change", (e) => {
        const inputElement = e.target;
        const children = inputElement.parentNode.children;
        const endTimeDate = inputElement.valueAsDate;
        const startTimeDate = new Date(children["ISO-start-time"].value);

        startTimeDate.setHours(endTimeDate.getHours())
        startTimeDate.setMinutes(endTimeDate.getMinutes())
        children["ISO-end-time"].value = startTimeDate.toISOString();
    });

    newRow.appendChild(startTime);
    newRow.appendChild(endTime);
    newRow.appendChild(ISOStartTime);
    newRow.appendChild(ISOEndTime);

    return newRow;
}

function addRow(row) {
    document.getElementById("availability-form").appendChild(row);
}

function addEmptyRow() {
    addRow(constructNewRow());
}