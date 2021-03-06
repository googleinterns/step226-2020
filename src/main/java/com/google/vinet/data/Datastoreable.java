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

package com.google.vinet.data;

/**
 * Any class that can be stored and loaded from Datastore. The implementing class must also provide
 * a constructor for loading an object from a Datastore entity.
 */
public interface Datastoreable {

  /**
   * Store this class instance to Datastore.
   *
   * @throws IllegalArgumentException                                     If the entity was incomplete.
   * @throws java.util.ConcurrentModificationException                    If the entity group to which the entity belongs was modified concurrently.
   * @throws com.google.appengine.api.datastore.DatastoreFailureException If any other datastore error occurs.
   */
  void toDatastore();
}
