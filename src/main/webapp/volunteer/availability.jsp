<!--
  ~  Copyright 2020 Google LLC
  ~
  ~  Licensed under the Apache License, Version 2.0 (the "License");
  ~  you may not use this file except in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~      https:www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the License is distributed on an "AS IS" BASIS,
  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~  See the License for the specific language governing permissions and
  ~  limitations under the License.
  -->

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Volunteer Matcher</title>
    <%@ include file="/bootstrap-css.html" %>
    <script src="availability.js"></script>
</head>
<body onload="initialise();">
<script src="availability.js"></script>
<h2>Welcome to volunteer matching!</h2>
<h2 class="alert alert-warning">Note: Availability can only be posted until 8pm each day.</h2>
<p>Please specify the time(s) in which you would be available to help:</p>
<br>
<form id="availability-form" method="POST" action="/volunteer-availability">
        <button type="button" class="btn btn-primary m-1" onclick="addEmptyRow()">Add Row</button>
        <input class="btn btn-primary m-1" type="submit" id="availability-submit">
</form>

</body>
<!-- Include necessary js libraries for bootstrap functionality. -->
<%@ include file="/bootstrap-js.html" %>
</html>

