// Double list selection JS, following 
// http://www.mredkj.com/tutorials/tutorial_mixed2b.html

var DoubleList = function() {
  
  function $it(elementID) {
    return document.getElementById(elementID);
  }
  // Compute the ID of the "shadow" submission field that is dynamically
  // maintained to back the left-hand list submission  
  function computeID(container, value) {
    return container.id + "$$$$double-list-select$$$$" + value;
    }
   
  function createInput(container, name, value) {
    var el = document.createElement("input");
    el.setAttribute("type", "hidden");
    el.setAttribute("name", name);
    el.setAttribute("id", computeID(container, value));
    el.setAttribute("value", value);  
    container.appendChild(el);
    YAHOO.log("Created input id " + el.id);
    }
    
  function deleteInput(container, name, value) {
    var el = $it(computeID(container, value));
    el.parentNode.removeChild(el);
    YAHOO.log("Deleted input id " + el.id);
    }
  
  function addOption(theSel, theText, theValue) {
    var newOpt = new Option(theText, theValue);
    var selLength = theSel.length;
    theSel.options[selLength] = newOpt;
    }

  function deleteOption(theSel, theIndex) {
    var selLength = theSel.length;
    if (selLength>0) {
      theSel.options[theIndex] = null;
      }
    }

  function moveOptions(theSelFrom, theSelTo, container, submitname, leftNum, all) {
    var selLength = theSelFrom.length;
    var selectedText = new Array();
    var selectedValues = new Array();
    var selectedCount = 0;
  
    var i;
  
  // Find the selected Options in reverse order
  // and delete them from the 'from' Select.
    for (i = selLength - 1; i >= 0; i--) {
      if (theSelFrom.options[i].selected || all) {
        var name = theSelFrom.options[i].text;
        var value = theSelFrom.options[i].value;
        selectedText[selectedCount] = name;
        selectedValues[selectedCount] = value;
        deleteOption(theSelFrom, i);
        if (leftNum == 1) {
          deleteInput(container, submitname, value);
          }
        selectedCount++;
        }
      }
  
    // Add the selected text/values in reverse order.
    // This will add the Options to the 'to' Select
    // in the same order as they were in the 'from' Select.
    for (i = selectedCount - 1; i >= 0; i--)  {
      addOption(theSelTo, selectedText[i], selectedValues[i]);
      if (leftNum == 2) {
        createInput(container, submitname, selectedValues[i]);
        }
      }
    }
  
  // See http://www.quirksmode.org/dom/w3c_core.html for poor compatibility
  // situation for removeAttribute
  function removeAttribute(element, name) {
    try {
      element.removeAttribute(name);
      }
    catch (e) {}
    // we're really only catching Opera 8, by my reading
    if (element.getAttribute(name)) { 
      element.removeAttributeNode(element.getAttributeNode(name));
      }
    }
    
  return {
    init_DoubleList: function(nameBase) {
      var container = $it(nameBase);
      var leftSel = $it(nameBase + "list1-selection");
      var rightSel = $it(nameBase + "list2-selection");
     
      var submitname = leftSel.getAttribute("name");
      // Prevent the real control from submitting anything, in case the user
      // has left some items selected
      removeAttribute(leftSel, "name");
      YAHOO.log("Container " + container + " nameBase " + nameBase + " submitname" + submitname);
      
      for (var i = 0; i < leftSel.length; ++ i) {
        var name = leftSel.options[i].text;
        var value = leftSel.options[i].value;
        createInput(container, submitname, value);
        }
      
      var moveRight = $it(nameBase + "move-right");
      var moveLeft = $it(nameBase + "move-left");
      var moveAllRight = $it(nameBase + "move-all-right");
      var moveAllLeft = $it(nameBase + "move-all-left");
            
      moveRight.onclick = function () {
        moveOptions(leftSel, rightSel, container, submitname, 1);
        return false;
        };
      moveLeft.onclick = function () {
        moveOptions(rightSel, leftSel, container, submitname, 2);
        return false;
        };
      moveAllRight.onclick = function () {
        moveOptions(leftSel, rightSel, container, submitname, 1, true);
        return false;
        };
      moveAllLeft.onclick = function () {
        moveOptions(rightSel, leftSel, container, submitname, 2, true);
        return false;
        };
      }
    };
  }();