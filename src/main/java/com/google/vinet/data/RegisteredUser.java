/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.vinet.data;

public class RegisteredUser {
  private final String userId;

  public RegisteredUser(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }

  @Override
  public int hashCode() {
    return userId.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (this.getClass() != obj.getClass()) return false;
    RegisteredUser registeredUser = ((RegisteredUser) obj);
    return userId.equals(registeredUser.userId);
  }
}
