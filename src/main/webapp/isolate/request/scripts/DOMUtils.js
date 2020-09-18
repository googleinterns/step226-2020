/**
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Remove an element from the DOM.
 * @param {!HTMLElement} element The element to be removed from the DOM.
 * @throws {TypeError} Will throw a TypeError if element is not viable for
 *     removal from the DOM.
 */
export const removeFromDOM = (element) => {
  const ERROR_PREFIX = `Element to be removed from DOM`;
  const TYPE_MISMATCH_PREFIX = `${ERROR_PREFIX} must not be`;
  const INVALID_PARENT_ELEMENT_MESSAGE =
      `${ERROR_PREFIX} has an invalid 'parentElement' attribute`;

  if (element === null) {
    throw (TypeError(`${TYPE_MISMATCH_PREFIX} null.`));
  }

  if (typeof element === 'undefined') {
    throw (TypeError(`${TYPE_MISMATCH_PREFIX} undefined.`));
  }

  let parent = null;

  // Ensure that the element has a parentElement attribute.
  try {
    parent = element.parentElement;
  } catch {
    throw (TypeError(`${ERROR_PREFIX} must have a 'parentElement' attribute`));
  }

  if (parent === null) {
    throw (TypeError(INVALID_PARENT_ELEMENT_MESSAGE));
  }

  if (typeof parent === 'undefined') {
    throw (TypeError(INVALID_PARENT_ELEMENT_MESSAGE));
  }

  /*
   * Try to remove the element from the DOM.
   * If this fails, the parent element must not be a valid element on the DOM.
   */
  try {
    parent.removeChild(element);
  } catch {
    throw (TypeError(INVALID_PARENT_ELEMENT_MESSAGE));
  }
};
