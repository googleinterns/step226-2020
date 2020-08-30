/**
 * Invert the visibility of an element of the page.
 * @param {HTMLElement} element The element whose visiblity will be inverted.
 * @throws {TypeError} Will throw a TypeError if the element is not viable for
 * having its visibility inverted.
 */
export const invertVisibility = (element) => {
  const ERROR_PREFIX = `Element whose invisibility is to be inverted`;
  const TYPE_MISMATCH_PREFIX = `${ERROR_PREFIX} must not be`;

  if (element === null) {
    throw (TypeError(`${TYPE_MISMATCH_PREFIX} null.`));
  }

  if (typeof element === 'undefined') {
    throw (TypeError(`${TYPE_MISMATCH_PREFIX} undefined.`));
  }

  try{
    element.hidden;
  } catch {
    throw (TypeError(`${ERROR_PREFIX} must have a 'hidden' attribute.`));
  }

  element.hidden = !element.hidden;
};
