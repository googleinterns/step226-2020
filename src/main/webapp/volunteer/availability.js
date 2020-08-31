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

function populateRows() {
    //TODO fetch existing timeslots for this volunteer from backend

    // add an empty row at bottom of form
    addEmptyRow();
}

function addEmptyRow() {
    const emptyRow = document.createElement("div");
    const date = document.createElement("input");
    date.type = "date";
    date.name = "date";

    const startTime = document.createElement("input");
    startTime.type = "time";
    startTime.name = "start-time";

    const endTime = document.createElement("input");
    endTime.type = "time";
    endTime.name = "end-time";

    emptyRow.appendChild(date);
    emptyRow.appendChild(startTime);
    emptyRow.appendChild(endTime);

    document.getElementById("availability-form").appendChild(emptyRow);
}