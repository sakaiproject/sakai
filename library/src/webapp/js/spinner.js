/*
 * For the purpose of this file, the terms 'disable' and 'clone and disable' refer
 * to the technique of first setting the element to display='none', so that it no
 * longer appears in the UI. The element is then 'cloned', copying all attributes,
 * classes, names/IDs, etc from the original and applying them to the clone. The
 * 'clone' is then set to disabled rather than the original.
 * 
 * This technique is necessary due to how forms send values of input elements
 * to the server when a form is submitted. If an input element is disabled when
 * the form is submitted, the value for that element is not sent to the server.
 * This can cause issues with form processing, etc.
 * 
 * The workaround is to perform the 'clone and disable' technique, described above,
 * to ensure that all input elements are sent to the server when the form is submitted,
 * but also ensure that the UI elements are no longer responsive to user input.
 * 
 * NOTE: when using the insertSpinnerAfter() function, it relies on specific (flex-box)
 * styles on the parent container in order to centre the spinner in line with the other
 * elements in the container. Simply adding the class 'spinnerBesideContainer' to the
 * container element should suffice to get it all lined up neatly.
 */

// Spinner namespace
var SPNR = SPNR || {};

// Get the main content div, return an empty div if it can't be found
const mainContent = () => {
  const contentElement = document.getElementById("content");
  return contentElement !== null ? contentElement : document.createElement("div");
};

/********** MAIN FUNCTIONS TO BE CALLED FROM OUTSIDE LIBRARY ******************/

/**
 * This function is to be used with elements that don't have enough area to overlay
 * a spinner, such as a drop down. It will (clone and) disable all elements on the
 * page and render a spinner graphic beside (the sibling of) the element being
 * interacted with (by default).
 * 
 * If you want the spinner graphic to appear somewhere other than beside the element
 * being interacted with, supply the ID of the parent element where you want the
 * spinner to appear. The function will append the spinner graphic as a child of the
 * requested element.
 * 
 * If no escapes and/or no override spinner location is needed, pass in null for their
 * respective parameters.
 * 
 * @param {DOM Element} clickedElement
 * @param {Array[String]} escapeList
 * @param {String} overrideSpinnerLocation
 */
SPNR.insertSpinnerAfter = function( clickedElement, escapeList, overrideSpinnerLocation )
{
    // Clone and disable all controls, passing the escape list
    SPNR.disableControlsAndSpin( clickedElement, escapeList );

    // Create the spinner graphic in a div
    const spinner = document.createElement( "div" );
    spinner.className = "spinPlaceholder";

    // If an override location for the spinner is desired, append it as a child...
    if( overrideSpinnerLocation && document.getElementById( overrideSpinnerLocation ) )
    {
        clickedElement = document.getElementById( overrideSpinnerLocation );
        clickedElement.appendChild( spinner );
    }

    // Otherwise, insert the spinner div as a sibling to the element being interacted with
    else
    {
        const parent = clickedElement.parentNode;
        parent.insertBefore( spinner, clickedElement.nextSibling );
    }
};

/**
 * This function is to be used in special cases where injecting an element into the DOM
 * causes 'bouncing' effects, where content is shifted dynamically.
 * 
 * In these special cases, you can add an empty div with a unique ID and the class
 * "allocatedSpinPlaceholder", so that the area is pre-allocated. This function will use
 * the pre-allocated div rather than injecting a div dynamically to avoid 'bouncing'.
 * 
 * If the div with the ID provided cannot be found, this will fallback to the
 * insertSpinnerAfter() algorithm.
 * 
 * If no escapes are needed, pass in null for the escapeList parameter.
 * 
 * @param {DOM Element} clickedElement - the element being interacted with
 * @param {Array[String]} escapeList - array of IDs you do not wish to be disabled
 * @param {String} allocatedID - the ID of the div manually added to the page to contain the spinner
 */
SPNR.insertSpinnerInPreallocated = function( clickedElement, escapeList, allocatedID )
{
    // Get the pre-allocated div for the spinner
    const div = document.getElementById( allocatedID );
    if( div !== null )
    {
        // Clone and disable all controls, passing the escape list
        SPNR.disableControlsAndSpin( clickedElement, escapeList );

        // Apply the class to get the spinner
        div.className = "spinPlaceholder";
    }

    // If the div couldn't be found, fallback to the insertSpinerAfter() algorithm
    else
    {
        SPNR.insertSpinnerAfter( clickedElement, escapeList, null );
    }
};

/**
 * This function is to be used with typical elements that have enough area to overlay
 * a spinner graphic on top. It will (clone and) disable all elements on the page,
 * and render a spinner graphic over top of the clicked element.
 * 
 * If no escapes are needed, pass in null for the escapeList parameter.
 * 
 * @param {DOM Element} clickedElement - the element being interacted with
 * @param {Array[String]} escapeList - array of IDs you do not wish to be disabled
 */
SPNR.disableControlsAndSpin = function( clickedElement, escapeList )
{
    if( escapeList === null )
    {
        escapeList = [];
    }

    // We want to disable buttons first, because when we clone inputs we make them buttons so the original inputs are still submitted with the form
    // (disabled inputs do not have their values sent to server on form submission)
    SPNR.disableButtons( clickedElement, escapeList );
    SPNR.disableInputs( clickedElement, escapeList );
    SPNR.disableLinkPointers( escapeList );
    SPNR.disableSelects( clickedElement, escapeList );
    SPNR.disableTextAreas( escapeList );
};

/*********************** UTILITY FUNCTIONS ************************************/

/**
 * This function will 'disable' all links (anchors) on the page. It will set the
 * 'pointerEvents' to none, apply the 'noPointers' class, and set the 'disabled'
 * attribute on the element itself.
 * 
 * @param {Array[String]} escapeList - array of IDs you do not wish to be disabled
 */
SPNR.disableLinkPointers = function( escapeList )
{
    // Remove cursor pointers for all links (rendering them unclickable)
    const links = mainContent().querySelectorAll( "a" );
    for( i = 0; i < links.length; i++ )
    {
        // Remove the pointer if it's not in the escape list
        if( escapeList.indexOf( links[i].id ) === -1 )
        {
            links[i].style.pointerEvents = "none";
            links[i].className += " noPointers";
            links[i].disabled = true;
        }
    }
};

/**
 * This function will 'disable' all input elements of the following types: submit,
 * button, checkbox, and text. The element that's being interacted with is passed
 * into the function, and a spinner will be overlayed on top of the element.
 * 
 * If no escapes are needed, pass in null for the escapeList parameter.
 * 
 * @param {DOM Element} clickedElement - the element being interacted with
 * @param {Array[String]} escapeList - array of IDs you do not wish to be disabled
 */
SPNR.disableInputs = function( clickedElement, escapeList )
{
    // Get all the input elements, separate into lists by type
    const allInputs = mainContent().querySelectorAll( "input" );
    for( i = 0; i < allInputs.length; i++ )
    {
        if( (allInputs[i].type === "submit" || allInputs[i].type === "button" || allInputs[i].type === "checkbox" || allInputs[i].type === "radio") 
                && escapeList.indexOf( allInputs[i].id ) === -1 )
        {
            // If this is the clicked button, activate the spinner on it; otherwise just disable the button
            if( allInputs[i] === clickedElement )
            {
                setTimeout(SPNR.disableElementAndSpin, 0, clickedElement.parentNode.id, clickedElement, true );
            }
            else
            {
                SPNR.disableElementAndSpin( "", allInputs[i], false );
            }
        }
        else if( allInputs[i].type === "text" && (escapeList === null || escapeList.indexOf( allInputs[i].id ) === -1) )
        {
            allInputs[i].readOnly = true;
        }
    }
};

/**
 * This function will 'disable' all button elements. The element that's being interacted with is passed
 * into the function, and a spinner will be overlayed on top of the element.
 *
 * If no escapes are needed, pass in null for the escapeList parameter.
 *
 * @param {DOM Element} clickedElement - the element being interacted with
 * @param {Array[String]} escapeList - array of IDs you do not wish to be disabled
 */
SPNR.disableButtons = function( clickedElement, escapeList )
{
    // Get all the button elements
    const buttons = mainContent().querySelectorAll( "button" );
    for( i = 0; i < buttons.length; i++ )
    {
        // Only disable if the button is not in the escape list
        if( escapeList.indexOf( buttons[i].id ) === -1 )
        {
            // If this is the clicked button, activate the spinner on it; otherwise just disable the button
            const activateSpinner = buttons[i] === clickedElement ? true : false;
            SPNR.disableElementAndSpin( "", buttons[i], activateSpinner );
        }
    }
};

/**
 * This function clones and disables all 'select' elements on the page.
 * 
 * @param {DOM Element} clickedElement - the element being interacted with
 * @param {Array[String]} escapeList - array of IDs you do not wish to be disabled
 */
SPNR.disableSelects = function( clickedElement, escapeList )
{
    // Clone and disable all drop downs (disable the clone, hide the original)
    const dropDowns = mainContent().querySelectorAll( "select" );
    for( i = 0; i < dropDowns.length; i++ )
    {
        // Only clone/disable if it's not in the escape list
        if( escapeList.indexOf( dropDowns[i].id ) === -1 )
        {
            // Hide the original drop down
            const select = dropDowns[i];
            select.style.display = "none";

            // Create the cloned element and disable it
            const newSelect = document.createElement( "select" );
            const oldId = select.getAttribute( "id" );
            let newId = (oldId !== null) ? oldId : Math.random().toString(36).substring(2, 15);
            newSelect.setAttribute( "id", newId + "Disabled" );
            newSelect.setAttribute( "name", select.getAttribute( "name" ) + "Disabled" );
            newSelect.setAttribute( "className", select.getAttribute( "className" ) );
            newSelect.setAttribute( "disabled", "true" );
            newSelect.size = select.size;
            newSelect.className = select.className;
            newSelect.innerHTML = select.innerHTML;
            newSelect.selectedIndex = select.selectedIndex;

            if (select === clickedElement)
            {
                // wrap it
                const wrapper = document.createElement("span");
                wrapper.className = "spinSelect";
                wrapper.appendChild(newSelect);
                // transfer any margins from the original select to the wrapper
                wrapper.style.margin = select.style.margin;

                // insert it
                select.parentNode.insertBefore(wrapper, select);
            }
            else
            {
                // Add the clone to the DOM where the original was
                select.parentNode.insertBefore( newSelect, select );
            }
        }
    }
};

/**
 * This function sets all 'textarea' elements on the page to read only, in effect
 * making them appear disabled.
 * 
 * @param {Array[String]} escapeList - array of IDs you do not wish to be disabled
 */
SPNR.disableTextAreas = function( escapeList )
{
    const allAreas = mainContent().querySelectorAll( "textarea" );
    for( i = 0; i < allAreas.length; ++i )
    {
        if( escapeList.indexOf( allAreas[i].id ) === -1 )
        {
            allAreas[i].readonly = true;
        }
    }
};

/**
 * This function is the 'meat and potatoes' of the new spinner process. It is
 * responsible for cloning and disabling 'input' elements, and will overlay the
 * spinner graphic on the element being interacted with.
 * 
 * @param {String} divID - the ID of the parent container
 * @param {DOM Element} element - the element being interacted with
 * @param {Boolean} activateSpinner - boolean flag to apply the spinner on the element or not
 */
SPNR.disableElementAndSpin = function( divID, element, activateSpinner )
{
    // First set the button to be invisible
    const origDisplay = element.style.display;
    element.style.display = "none";

    // Now create a new disabled button with the same attributes as the existing button
    let newElement;
    if( element.type === "submit" || element.type === "button" )
    {
        newElement = document.createElement( "button" );
    }
    else
    {
        newElement = document.createElement( "input" );
        newElement.setAttribute( "type", element.type );
    }

    const oldId = element.getAttribute( "id" );
    let newId = (oldId !== null) ? oldId : Math.random().toString(36).substring(2, 15);
    newElement.setAttribute( "id", newId + "Disabled" );
    newElement.setAttribute( "name", element.getAttribute( "name" ) + "Disabled" );
    newElement.setAttribute( "value", element.getAttribute( "value" ) );
    newElement.setAttribute( "disabled", "true" );
    newElement.style.display = origDisplay;

    if( element.type === "checkbox" || element.type === "radio" )
    {
        newElement.checked = element.checked;
    }
    else
    {
        // <input>                --> localName = "input", nodeName="INPUT", tagName="INPUT", type="text"
        // <input type="submit">  --> localName = "input", nodeName="INPUT", tagName="INPUT", type="submit"
        // <button>               --> localName = "button", nodeName="button", tagName="BUTTON", type="submit"
        // <button type="button"> --> localName = "button", nodeName="button", tagName="BUTTON", type="button"
        element.tagName === "BUTTON" ? newElement.innerHTML = element.innerHTML : newElement.textContent = element.getAttribute( "value" );
        newElement.className = element.className + " " + (activateSpinner ? "btn spinButton formButtonDisabled" : "btn formButtonDisabled");
    }

    if( "" !== divID )
    {
        const div = document.getElementById( divID );
        div.insertBefore( newElement, element );
    }
    else
    {
        const parent = element.parentNode;
        parent.insertBefore( newElement, element );
    }
};
