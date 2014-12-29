/**********************************************************************************
* $HeadURL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/jsf/component/RichTextEditArea.java $
* $Id: RichTextEditArea.java 226 2005-06-23 23:46:26Z esmiley@stanford.edu $
***********************************************************************************
*
 * Copyright (c) 2005 The Sakai Foundation
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

//**************GET ELEMENT for diffrent Browsers***************************************************************

function getElm(thisid)
{
   var thiselm = null;
   if(document.getElementById)
   {
        //alert("getElementbyId");
      // browser implements part of W3C DOM HTML ( Gecko, Internet Explorer 5+, Opera 5+
      thiselm = document.getElementById(thisid);
   }
   else if(document.all)
   {
   		//alert("document.all");
      // Internet Explorer 4 or Opera with IE user agent
      thiselm = document.all[thisid];
   }
   else if(document.layers)
   {
   		//alert("document.layers");
      // Navigator 4
      thiselm = document.layers[thisid];
   }
   if(thiselm)
   {

      if(thiselm == null)
      {

         return;
      }
      else
      {

         return thiselm;
      }
   }
}
//-----------------------------------------------------------------------------------------------------------------
// functions to find enable / disable radio buttons
function enableDisableRadioButtons(nameRadio, action)
		 {
 			  var chkelms = new Array(); // define an null array
   			var chkelms =getelmbytag('input'); // get all the input elements on this page
    		var elmnum = chkelms.length; // set the number of input elements
      for (var i=0; i < elmnum; i++) // loop through each one of input element to check name and then set disabled...
      if(chkelms[i].name == nameRadio && chkelms[i].type =="radio" ) {
        chkelms[i].disabled = action;
      }
     }


//-----------------------------------------------------------------------------------------------------------------
// functions to find all elements for specific tag name and name attribute
function getelmbytag(tag)
{
 var elm = new Array();

 if (document.getElementsByTagName(tag)) {
              // browser implements part of W3C DOM HTML ( Gecko, Internet Explorer 5+, Opera 5+
    elm = document.getElementsByTagName(tag);  }

    else if (document.all){
        elm = document.all[tag];
   //Internet Explorer 4 or Opera with IE user agent
           }

     else if (document.layers){ // Navigator 4
            elm = document.layers[tag];
      }

   if (elm)  { // Browser supports elm
      return elm;
     }
 else { return;}
}


function move(fbox, tbox)
{
   var arrFbox = new Array();
   var arrTbox = new Array();
   var arrLookup = new Array();
   var i;
   for(i = 0; i < tbox.options.length; i++)
   {
      arrLookup[tbox.options[i].text] = tbox.options[i].value;
      arrTbox[i] = tbox.options[i].text;
   }
   var fLength = 0;
   var tLength = arrTbox.length;
   for(i = 0; i < fbox.options.length; i++)
   {
      arrLookup[fbox.options[i].text] = fbox.options[i].value;
      if(fbox.options[i].selected && fbox.options[i].value != "")
      {
         arrTbox[tLength] = fbox.options[i].text;
         tLength++;
      }
      else
      {
         arrFbox[fLength] = fbox.options[i].text;
         fLength++;
      }
   }
   //  arrFbox.sort();
   //arrTbox.sort();
   fbox.length = 0;
   tbox.length = 0;
   var c;
   for(c = 0; c < arrFbox.length; c++)
   {
      var no = new Option();
      no.value = arrLookup[arrFbox[c]];
      no.text = arrFbox[c];
      fbox[c] = no;
   }
   for(c = 0; c < arrTbox.length; c++)
   {
      var no = new Option();
      no.value = arrLookup[arrTbox[c]];
      no.text = arrTbox[c];
      tbox[c] = no;
   }
}

function getSelectedRadio(buttonGroup)
{
   // returns the array number of the selected radio button or -1 if no button is selected
   if(buttonGroup[0])
   {
      // if the button group is an array (one button is not an array)
      for(var i = 0; i < buttonGroup.length; i++)
      {
         if(buttonGroup[i].checked)
         {
            return i
         }
      }
   }
   else
   {
      if(buttonGroup.checked)
      {
         return 0;
      }
      // if the one button is checked, return zero
   }
   // if we get to this point, no radio button is selected
   return - 1;
}
// Ends the "getSelectedRadio" function

function getSelectedRadioValue(buttonGroup)
{
   // returns the value of the selected radio button or "" if no button is selected
   var i = getSelectedRadio(buttonGroup);
   if(i == - 1)
   {
      return "";
   }
   else
   {
      if(buttonGroup[i])
      {
         // Make sure the button group is an array (not just one button)
         return buttonGroup[i].value;
      }
      else
      {
         // The button group is just the one button, and it is checked
         return buttonGroup.value;
      }
   }
}
// Ends the "getSelectedRadioValue" function

function getSelectedCheckbox(buttonGroup)
{
   // Go through all the check boxes. return an array of all the ones
   // that are selected (their position numbers). if no boxes were checked,
   // returned array will be empty (length will be zero)
   var retArr = new Array();
   var lastElement = 0;
   if(buttonGroup[0])
   {
      // if the button group is an array (one check box is not an array)
      for(var i = 0; i < buttonGroup.length; i++)
      {
         if(buttonGroup[i].checked)
         {
            retArr.length = lastElement;
            retArr[lastElement] = i;
            lastElement++;
         }
      }
   }
   else
   {
      // There is only one check box (it's not an array)
      if(buttonGroup.checked)
      {
         // if the one check box is checked
         retArr.length = lastElement;
         retArr[lastElement] = 0;
         // return zero as the only array value
      }
   }
   return retArr;
}
// Ends the "getSelectedCheckbox" function

function getSelectedCheckboxValue(buttonGroup)
{
   // return an array of values selected in the check box group. if no boxes
   // were checked, returned array will be empty (length will be zero)
   var retArr = new Array();
   // set up empty array for the return values
   var selectedItems = getSelectedCheckbox(buttonGroup);
   if(selectedItems.length != 0)
   {
      // if there was something selected
      retArr.length = selectedItems.length;
      for(var i = 0; i < selectedItems.length; i++)
      {
         if(buttonGroup[selectedItems[i]])
         {
            // Make sure it's an array
            retArr[i] = buttonGroup[selectedItems[i]].value;
         }
         else
         {
            // It's not an array (there's just one check box and it's selected)
            retArr[i] = buttonGroup.value;
            // return that value
         }
      }
   }
   return retArr;
}
// Ends the "getSelectedCheckBoxValue" function

function getFieldValue(field)
{
   switch(field.type)
   {
      case "text" :
      case "textarea" :
      case "password" :
      case "hidden" :
         return field.value;
      case "select-one" :
         var i = field.selectedIndex;
         if(i == - 1)
         return "";
         else
         return(field.options[i].value == "") ? field.options[i].text : field.options[i].value;
      case "select-multiple" :
         var allChecked = new Array();
         for(i = 0; i < field.options.length; i++)
         if(field.options[i].selected) allChecked[allChecked.length] =(field.options[i].value == "") ? field.options[i].text : field.options[i].value;
         return allChecked;
      case "button" :
      case "reset" :
      case "submit" :
         return "";
      case "radio" :
      case "checkbox" :
         if(field.checked)
         {
            return field.value;
         }
         else
         {
            return "";
         }
      default :
         if(field[0].type == "radio")
         {
            for(i = 0; i < field.length; i++)
            if(field[i].checked)
            return field[i].value;
            return "";
         }
         else if(field[0].type == "checkbox")
         {
            var allChecked = new Array();
            for(i = 0; i < field.length; i++)
            if(field[i].checked) allChecked[allChecked.length] = field[i].value;
            return allChecked;
         }
         else
         var str = "";
         for(x in field)
         {
            str += x + "\n";
         }
         alert("I couldn't figure out what type this field is...\n\n" + field.name + ": ???\n\n\n" + str + "\n\nlength = " + field.length);
         break;
   }
   return "";
}
/*
==================================================================
checkEmpty_focus(formElement, msg) : checks for empty element value and returns the focus to the field
==================================================================
 */

function checkEmpty_focus(formElement, msg)
{
   var isEmptyField = isEmptyElement(formElement);
   if(isEmptyField == true)
   {
      alert(msg);
      setFocus(formElement);
      return false;
   }
}
/*
==================================================================
isEmpty(string) : Returns true  or false for empty string
==================================================================
 */

function isEmptyElement(formElement)
{
   var element = getElm(formElement);
   element.value = Trim(element.value);
   if((element.value.length == 0) ||(element.value == null))
   {
      return true;
   }
   else
   {
      return false;
   }
}
/*
==================================================================
setFocus(string) : Sets focus to given field
==================================================================
 */

function setFocus(formElement)
{
   var element = getElm(formElement);
   element.focus();
   return false;
}
/*
==================================================================
LTrim(string) : Returns a copy of a string without leading spaces.
==================================================================
 */

function LTrim(str)
/*
   PURPOSE: Remove leading blanks from our string.
   IN: str - the string we want to LTrim
 */
{
   var whitespace = new String(" \t\n\r");
   var s = new String(str);
   if(whitespace.indexOf(s.charAt(0)) != - 1)
   {
      // We have a string with leading blank(s)...
      var j = 0, i = s.length;
      // Iterate from the far left of string until we
      // don't have any more whitespace...
      while(j < i && whitespace.indexOf(s.charAt(j)) != - 1) j++;
      // Get the substring from the first non-whitespace
      // character to the end of the string...
      s = s.substring(j, i);
   }
   return s;
}
/*
==================================================================
RTrim(string) : Returns a copy of a string without trailing spaces.
==================================================================
 */

function RTrim(str)
/*
   PURPOSE: Remove trailing blanks from our string.
   IN: str - the string we want to RTrim

 */
{
   // We don't want to trip JUST spaces, but also tabs,
   // line feeds, etc.  Add anything else you want to
   // "trim" here in Whitespace
   var whitespace = new String(" \t\n\r");
   var s = new String(str);
   if(whitespace.indexOf(s.charAt(s.length - 1)) != - 1)
   {
      // We have a string with trailing blank(s)...
      var i = s.length - 1;
      // Get length of string
      // Iterate from the far right of string until we
      // don't have any more whitespace...
      while(i >= 0 && whitespace.indexOf(s.charAt(i)) != - 1) i--;
      // Get the substring from the front of the string to
      // where the last non-whitespace character is...
      s = s.substring(0, i + 1);
   }
   return s;
}
/*
=============================================================
Trim(string) : Returns a copy of a string without leading or trailing spaces
=============================================================
 */

function Trim(str)
/*
   PURPOSE: Remove trailing and leading blanks from our string.
   IN: str - the string we want to Trim

   RETVAL: A Trimmed string!
 */
{
   return RTrim(LTrim(str));
}
//-------------------------------------------------------------------
// isBlank(value)
//   Returns true if value only contains spaces
//-------------------------------------------------------------------

function isBlank(val)
{
   if(val == null)
   {
      return true;
   }
   for(var i = 0; i < val.length; i++)
   {
      if((val.charAt(i) != ' ') &&(val.charAt(i) != "\t") &&(val.charAt(i) != "\n") &&(val.charAt(i) != "\r"))
      {
         return false;
      }
   }
   return true;
}
//-------------------------------------------------------------------
// isInteger(value)
//   Returns true if value contains all digits
//-------------------------------------------------------------------

function isInteger(val)
{
   if(isBlank(val))
   {
      return false;
   }
   for(var i = 0; i < val.length; i++)
   {
      if(!isDigit(val.charAt(i)))
      {
         return false;
      }
   }
   return true;
}
//-------------------------------------------------------------------
// isNumeric(value)
//   Returns true if value contains a positive float value
//-------------------------------------------------------------------

function isNumeric(val)
{
   return(parseFloat(val, 10) ==(val * 1));
}
//-------------------------------------------------------------------
// isArray(obj)
// Returns true if the object is an array, else false
//-------------------------------------------------------------------

function isArray(obj)
{
   return( typeof(obj.length) == "undefined") ? false : true;
}
//-------------------------------------------------------------------
// isDigit(value)
//   Returns true if value is a 1-character digit
//-------------------------------------------------------------------

function isDigit(num)
{
   if(num.length > 1)
   {
      return false;
   }
   var string = "1234567890";
   if(string.indexOf(num) != - 1)
   {
      return true;
   }
   return false;
}
//-------------------------------------------------------------------
// setNullIfBlank(input_object)
//   Sets a form field to "" if it isBlank()
//-------------------------------------------------------------------

function setNullIfBlank(obj)
{
   if(isBlank(obj.value))
   {
      obj.value = "";
   }
}
//-------------------------------------------------------------------
// setFieldsToUpperCase(input_object)
//   Sets value of form field toUpperCase() for all fields passed
//-------------------------------------------------------------------

function setFieldsToUpperCase()
{
   for(var i = 0; i < arguments.length; i++)
   {
      arguments[i].value = arguments[i].value.toUpperCase();
   }
}
//-------------------------------------------------------------------
// disallowBlank(input_object[,message[,true]])
//   Checks a form field for a blank value. Optionally alerts if
//   blank and focuses
//-------------------------------------------------------------------

function disallowBlank(obj)
{
   var msg =(arguments.length > 1) ? arguments[1] : "";
   var dofocus =(arguments.length > 2) ? arguments[2] : false;
   if(isBlank(getInputValue(obj)))
   {
      if(!isBlank(msg))
      {
         alert(msg);
      }
      if(dofocus)
      {
         if(isArray(obj) &&( typeof(obj.type) == "undefined"))
         {
            obj = obj[0];
         }
         if(obj.type == "text" || obj.type == "textarea" || obj.type == "password")
         {
            obj.select();
         }
         obj.focus();
      }
      return true;
   }
   return false;
}
//-------------------------------------------------------------------
// disallowModify(input_object[,message[,true]])
//   Checks a form field for a value different than defaultValue.
//   Optionally alerts and focuses
//-------------------------------------------------------------------

function disallowModify(obj)
{
   var msg =(arguments.length > 1) ? arguments[1] : "";
   var dofocus =(arguments.length > 2) ? arguments[2] : false;
   if(getInputValue(obj) != getInputDefaultValue(obj))
   {
      if(!isBlank(msg))
      {
         alert(msg);
      }
      if(dofocus)
      {
         if(isArray(obj) &&( typeof(obj.type) == "undefined"))
         {
            obj = obj[0];
         }
         if(obj.type == "text" || obj.type == "textarea" || obj.type == "password")
         {
            obj.select();
         }
         obj.focus();
      }
      setInputValue(obj, getInputDefaultValue(obj));
      return true;
   }
   return false;
}
//-------------------------------------------------------------------
// commifyArray(array[,delimiter])
//   Take an array of values and turn it into a comma-separated string
//   Pass an optional second argument to specify a delimiter other than
//   comma.
//-------------------------------------------------------------------

function commifyArray(obj, delimiter)
{
   if( typeof(delimiter) == "undefined" || delimiter == null)
   {
      delimiter = ",";
   }
   var s = "";
   if(obj == null || obj.length <= 0)
   {
      return s;
   }
   for(var i = 0; i < obj.length; i++)
   {
      s = s +((s == "") ? "" : delimiter) + obj[i].toString();
   }
   return s;
}
