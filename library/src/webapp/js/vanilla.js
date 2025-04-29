/**********************************************************************************
 * $URL:
 * $Id:
 ***********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

/* jshint esversion: 8 */

/**
 * Vanilla class - Contains helper functions for vanilla js functionality
 */
class Vanilla {
  /**
   * DOM ready
   * @param {Function} callback - Function to execute when DOM is ready
   */
  static domReady(callback) {
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', callback);
    } else {
      callback();
    }
  }

  /**
   * Add class
   * @param {Element} element - Target element
   * @param {String} className - Class to add
   */
  static addClass(element, className) {
    if (element && className) {
      const classes = className.split(' ');
      for (const cls of classes) {
        if (cls) {
          element.classList.add(cls);
        }
      }
    }
  }

  /**
   * Remove class
   * @param {Element} element - Target element
   * @param {String} className - Class to remove
   */
  static removeClass(element, className) {
    if (element && className) {
      const classes = className.split(' ');
      for (const cls of classes) {
        if (cls) {
          element.classList.remove(cls);
        }
      }
    }
  }

  /**
   * Has class
   * @param {Element} element - Target element
   * @param {String} className - Class to check
   * @returns {Boolean} - True if element has class
   */
  static hasClass(element, className) {
    if (!element || !className) {
      return false;
    }
    const classes = className.split(' ');
    for (const cls of classes) {
      if (cls && element.classList.contains(cls)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Set/get attribute
   * @param {Element} element - Target element
   * @param {String} name - Attribute name
   * @param {String} value - Attribute value
   * @returns {String|Element} - Attribute value or element
   */
  static attr(element, name, value) {
    if (!element) {
      return null;
    }
    if (value === undefined) {
      return element.getAttribute(name);
    }
    element.setAttribute(name, value);
    return element;
  }

  /**
   * Remove attribute
   * @param {Element} element - Target element
   * @param {String} name - Attribute name
   */
  static removeAttr(element, name) {
    if (element) {
      element.removeAttribute(name);
    }
  }

  /**
   * HTML content
   * @param {Element} element - Target element
   * @param {String} content - HTML content
   * @returns {String|Element} - HTML content or element
   */
  static html(element, content) {
    if (!element) {
      return null;
    }
    if (content === undefined) {
      return element.innerHTML;
    }
    element.innerHTML = content;
    return element;
  }

  /**
   * Text content
   * @param {Element} element - Target element
   * @param {String} content - Text content
   * @returns {String|Element} - Text content or element
   */
  static text(element, content) {
    if (!element) {
      return null;
    }
    if (content === undefined) {
      return element.textContent;
    }
    element.textContent = content;
    return element;
  }

  /**
   * Value for form elements
   * @param {Element} element - Target element
   * @param {String} value - Value
   * @returns {String|Element} - Value or element
   */
  static val(element, value) {
    if (!element) {
      return null;
    }
    if (value === undefined) {
      return element.value;
    }
    element.value = value;
    return element;
  }

  /**
   * Append
   * @param {Element} parent - Parent element
   * @param {Element|String} child - Child element or HTML string
   * @returns {Element} - Parent element
   */
  static append(parent, child) {
    if (parent && child) {
      if (typeof child === 'string') {
        parent.insertAdjacentHTML('beforeend', child);
      } else {
        parent.appendChild(child);
      }
    }
    return parent;
  }

  /**
   * Remove element
   * @param {Element} element - Element to remove
   */
  static remove(element) {
    if (element && element.parentNode) {
      element.parentNode.removeChild(element);
    }
  }

  /**
   * Event listener
   * @param {Element} element - Target element
   * @param {String} event - Event name
   * @param {Function} handler - Event handler
   * @param {Object|Boolean} options - Event options
   * @returns {Element} - Target element
   */
  static on(element, event, handler, options = false) {
    if (element) {
      element.addEventListener(event, handler, options);
    }
    return element;
  }

  /**
   * Trigger event
   * @param {Element} element - Target element
   * @param {String} eventName - Event name
   * @returns {Element} - Target element
   */
  static trigger(element, eventName) {
      const event = new Event(eventName, { bubbles: true });
      element.dispatchEvent(event);
      return element;
  }

  /**
   * Animation (simple fade out) using Bootstrap's fade classes
   * @param {Element} element - Target element
   * @param {Number} duration - Animation duration
   * @param {Function} callback - Callback function
   */
  static fadeOut(element, duration, callback = null) {
    if (!element) {
      return;
    }

    if (duration === undefined) {
      duration = 300;
    }

    // Add Bootstrap fade classes
    Vanilla.addClass(element, 'fade');

    // Set transition duration
    element.style.transition = `opacity ${duration}ms`;

    // Start the fade out
    Vanilla.removeClass(element, 'show');

    // Hide the element after the animation completes
    setTimeout(() => {
      element.style.display = 'none';
      if (callback) {
        callback();
      }
    }, duration);
  }

  /**
   * Animation (simple fade in) using Bootstrap's fade classes
   * @param {Element} element - Target element
   * @param {Number} duration - Animation duration
   * @param {String} display - Display value
   * @param {Function} callback - Callback function
   */
  static fadeIn(element, duration, display, callback = null) {
    if (!element) {
      return;
    }

    if (duration === undefined) {
      duration = 300;
    }

    if (display === undefined) {
      display = 'block';
    }

    // Add Bootstrap fade class if not already present
    Vanilla.addClass(element, 'fade');

    // Set display property
    element.style.display = display;

    // Set transition duration
    element.style.transition = `opacity ${duration}ms`;

    // Force a reflow to ensure the transition works
    // Using void to suppress JSHint warning about expression without assignment
    void element.offsetHeight;

    // Add the show class to trigger the fade in
    Vanilla.addClass(element, 'show');

    // Call the callback after the animation completes
    setTimeout(() => {
      if (callback) {
        callback();
      }
    }, duration);
  }

  /**
   * AJAX
   * @param {Object} options - AJAX options
   * @returns {Promise} - Promise object
   * 
   * Note: This function uses ES8 features (async/await)
   */
  static async ajax(options) {
    const { 
      url: originalUrl, 
      type = 'GET', 
      data = null, 
      contentType = 'application/json', 
      beforeSend, 
      success, 
      error, 
      cache = true 
    } = options;

    // Create a mutable copy of the URL
    let requestUrl = originalUrl;

    // Prepare fetch options
    const fetchOptions = {
      method: type,
      headers: {},
      // Don't set credentials by default to maintain backward compatibility
      credentials: 'same-origin'
    };

    // Set content type header if provided
    if (contentType) {
      fetchOptions.headers['Content-Type'] = contentType;
    }

    // Set cache control if needed
    if (!cache) {
      fetchOptions.headers['Cache-Control'] = 'no-cache';
    }

    // Prepare request data
    if (data) {
      if (type === 'GET') {
        // For GET requests, append data to URL as query parameters
        const queryParams = new URLSearchParams();
        if (typeof data === 'object') {
          Object.keys(data).forEach(key => {
            if (data[key] !== null && data[key] !== undefined) {
              queryParams.append(key, data[key]);
            }
          });
        }
        const queryString = queryParams.toString();
        if (queryString) {
          const separator = requestUrl.includes('?') ? '&' : '?';
          requestUrl = `${requestUrl}${separator}${queryString}`;
        }
      } else {
        // For non-GET requests, set the body
        if (data instanceof FormData) {
          fetchOptions.body = data;
        } else if (typeof data === 'object' && contentType.includes('json')) {
          fetchOptions.body = JSON.stringify(data);
        } else if (contentType.includes('x-www-form-urlencoded')) {
          // Handle form-urlencoded data
          const formData = new URLSearchParams();
          if (typeof data === 'object') {
            Object.keys(data).forEach(key => {
              if (data[key] !== null && data[key] !== undefined) {
                formData.append(key, data[key]);
              }
            });
          }
          fetchOptions.body = formData.toString();
        } else {
          // Handle other data types
          fetchOptions.body = data;
        }
      }
    }

    try {
      // Call beforeSend if provided
      if (beforeSend) {
        beforeSend({ status: 0, statusText: '' });
      }

      // Make the fetch request
      const response = await fetch(requestUrl, fetchOptions);

      // Get response text
      const responseText = await response.text();

      // Try to parse as JSON if possible
      let parsedResponse;
      try {
        parsedResponse = JSON.parse(responseText);
      } catch (e) {
        parsedResponse = responseText;
      }

      // Handle success
      if (response.ok) {
        if (success) {
          success(parsedResponse, response.statusText, { 
            status: response.status, 
            statusText: response.statusText, 
            responseText 
          });
        }
        return parsedResponse;
      } else {
        // Handle error response
        if (error) {
          error({ 
            status: response.status, 
            statusText: response.statusText, 
            responseText 
          }, response.statusText);
        }
        throw { status: response.status, statusText: response.statusText };
      }
    } catch (err) {
      // Handle network errors or thrown errors
      if (error) {
        error({ 
          status: err.status || 0, 
          statusText: err.statusText || 'Network Error', 
          responseText: '' 
        }, err.statusText || 'Network Error');
      }
      throw err;
    }
  }
}

// Export individual functions for easier use
export const addClass = Vanilla.addClass;
export const removeClass = Vanilla.removeClass;
export const hasClass = Vanilla.hasClass;
export const attr = Vanilla.attr;
export const removeAttr = Vanilla.removeAttr;
export const html = Vanilla.html;
export const text = Vanilla.text;
export const val = Vanilla.val;
export const append = Vanilla.append;
export const remove = Vanilla.remove;
export const on = Vanilla.on;
export const trigger = Vanilla.trigger;
export const fadeOut = Vanilla.fadeOut;
export const fadeIn = Vanilla.fadeIn;
export const ajax = Vanilla.ajax;
export const domReady = Vanilla.domReady;
