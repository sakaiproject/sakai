/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
 
 /**
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
 
// moves all the items to the selected list
function myfaces_picklist_addAllToSelected(availableListId, selectedListId, hiddenId)
{
	var availableList = document.getElementById(availableListId);
    var selectedList = document.getElementById(selectedListId);

	myfaces_picklist_moveAll(availableList, selectedList, hiddenId);
	myfaces_picklist_updateHidden(selectedList, hiddenId);
}

// removes all the items from the selected list
function myfaces_picklist_removeAllFromSelected(availableListId, selectedListId, hiddenId)
{
    var availableList = document.getElementById(availableListId);
    var selectedList = document.getElementById(selectedListId);

	myfaces_picklist_moveAll(selectedList, availableList, hiddenId);
	myfaces_picklist_updateHidden(selectedList, hiddenId);
}

// moves an item to the selected list
function myfaces_picklist_addToSelected(availableListId, selectedListId, hiddenId)
{
	var availableList = document.getElementById(availableListId);
    var selectedList = document.getElementById(selectedListId);

	myfaces_picklist_move(availableList, selectedList, hiddenId);
	myfaces_picklist_updateHidden(selectedList, hiddenId);
}

// removes an item from the selected list
function myfaces_picklist_removeFromSelected(availableListId, selectedListId, hiddenId)
{
    var availableList = document.getElementById(availableListId);
    var selectedList = document.getElementById(selectedListId);

	myfaces_picklist_move(selectedList, availableList, hiddenId);
	myfaces_picklist_updateHidden(selectedList, hiddenId);
}

function myfaces_picklist_move(fromList, toList, hiddenId) {
	// Return, if no items selected
	var selectedIndex = fromList.selectedIndex;
	if(selectedIndex < 0) { return; }

	// Decremental loop, so the index is not affected in the moves
	for (var i = fromList.options.length - 1; i >= 0; i--) {
		if(fromList.options[i].selected) {
			var tLen = toList.options.length;
			toList.options[tLen] = new Option(fromList.options[i].text);
			toList.options[tLen].value = fromList.options[i].value;
			fromList.options[i] = null;
		}
	}
}

function myfaces_picklist_moveAll(fromList, toList, hiddenId) {

	// Decremental loop, so the index is not affected in the moves
	for (var i = fromList.options.length - 1; i >= 0; i--) {
		var tLen = toList.options.length;
		toList.options[tLen] = new Option(fromList.options[i].text);
		toList.options[tLen].value = fromList.options[i].value;
		fromList.options[i] = null;
	}
}

// Selection - invoked on submit
function myfaces_picklist_updateHidden(selectedList, hiddenId) {
	var hiddenField = document.getElementById(hiddenId);
	
	var arrValues = new Array(selectedList.options.length);
	for (var i = 0; i<selectedList.options.length; i++) {
	    arrValues[i] = selectedList.options[i].value;
	}
	
	hiddenField.value = arrValues.join();
}