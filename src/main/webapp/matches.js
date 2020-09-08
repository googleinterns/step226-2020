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
    displayMatches();
}

async function displayMatches() {
    const matches = await getMatches();
    console.log(matches);
    const table = document.getElementById("matches-table");
    matches.forEach(match => addTableRow(table, match));
}

async function getMatches() {
    return (await fetch('/match-fetcher')).json();
}

function addTableRow(table, match) {
    console.log("Match: ", match);
    const row = table.insertRow();

    Object.keys(match).forEach(key => {
        let value = match[key];
        if (key === "date") value = new Date(match[key]).toLocaleDateString();
        if (key === "start" || key === "end") value = new Date(match[key]).toLocaleTimeString();
        row.insertCell().appendChild(document.createTextNode(value))
    });
}