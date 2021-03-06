
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
    <title>Admin Matching Dashboard</title>
    <%@ include file="/bootstrap-css.html" %>
    <script src="scripts/main.js" type="module"></script>
</head>
<body>
  <div class="container">
    <div class="row">
      <div class="column">
        <button onclick="runMatching()" class="btn btn-primary">Run Matching</button>
        <h1 id="status" class="alert"></h1>
      </div>
    </div>
  </div>
</body>
<!-- Include necessary js libraries for bootstrap functionality. -->
<%@ include file="/bootstrap-js.html" %>
</html>
