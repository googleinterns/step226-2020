<!--
  ~ Copyright 2020 Google LLC
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     https://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
  <title>Registration</title>
  <%@ include file="/bootstrap-css.html" %>
  <script src="maps.js"></script>
  <script defer
      src="https://maps.googleapis.com/maps/api/js?key=<API_KEY_HERE>&callback=loadMap&libraries=places">
  </script>
</head>
<body>
  <div class="container" id="container">
    <div class="row">
      <h2>
        Please fill out the form below to register with VINet.
      </h2>
    </div>
    <!-- Include the registration form on the server side using JSP -->
    <%@ include file = "registration-form.html" %>
  </div>
</body>
<!-- Include necessary js libraries for bootstrap functionality. -->
<%@ include file="/bootstrap-js.html" %>
</html>
