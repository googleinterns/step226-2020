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
    <title>Your Requests</title>
    <%@ include file="/bootstrap-css.html" %>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.27.0/moment.min.js" integrity="sha512-rmZcZsyhe0/MAjquhTgiUcb4d9knaFc7b5xAfju483gbEXTkeJRUMIPk6s3ySZMYUHEcjKbjLjyddGWMrNEvZg==" crossorigin="anonymous"></script>
    <script src="/scripts/HTTPStatusCodes.js"></script>
    <script src="requests.js"></script>
</head>
<body>
    <div id="container" class="container-fluid">
        <table id="requests" class="table table-striped">
            <thead class="thead-dark">
                <th>
                    Date
                </th>
                <th>
                    Start of Delivery Window
                </th>
                <th>
                    End of Delivery Window
                </th>
                <th>
                    Request Tickets
                </th>
            </thead>
        </table>
    </div>
</body>
<!-- Include necessary js libraries for bootstrap functionality. -->
<%@ include file="/bootstrap-js.html" %>
</html>
