/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
;(function (undefined) {
	'use strict';

	if (typeof(Wicket) === "undefined") {
		window.Wicket = {};
	}

	Wicket.Tree = {};

	Wicket.Tree.askForReload = function() {
		if (window.confirm("There was a problem updating the tree. It might be caused be the old page being cached by the browser. \n" +
			"It is recommended to reload the page. Do you want to reload it?"))
		{
			window.location.reload();
		}
	};

	Wicket.Tree.removeNodes = function(prefix, nodeList) {
		var problem = false;
		for (var i = 0; i < nodeList.length; i++) {
			var e = document.getElementById(prefix + nodeList[i]);
			if (e != null) {
				e.parentNode.removeChild(e);
			}
			else {
				// while developing alert a warning
				problem = true;
				if (Wicket.Log) {
					Wicket.Log.error("Can't find node with id " + prefix + nodeList[i] +
						". This shouldn't happen - possible bug in tree?");
				}
			}
		}

		if (problem === true) {
			Wicket.Tree.askForReload();
		}
	};

	Wicket.Tree.createElement = function(elementId, afterId) {
		var existing = Wicket.$(elementId);
		if (typeof(existing) !== "undefined" && existing !== null) {
			Wicket.Tree.askForReload();
		}

		var after = document.getElementById(afterId);
		var newNode = document.createElement(after.tagName);
		newNode.setAttribute("id", elementId);

		var p = after.parentNode;

		for (var i = 0; i < p.childNodes.length; ++i) {
			if (after === p.childNodes[i]) {
				break;
			}
		}
		if (i === p.childNodes.length - 1) {
			p.appendChild(newNode);
		}
		else {
			p.insertBefore(newNode, p.childNodes[i + 1]);
		}
	};

	Wicket.TreeTable = {};

	/* Javascript that resizes the tree table header so that it matches size of the content.
	   This is needed when the scrollbar next to content is show, so that the columns are
	   properly aligned */
	Wicket.TreeTable.update = function(elementId)
	{
		var element = document.getElementById(elementId);

		if (element !== null && typeof(element) !== "undefined")
		{
			try {
				// find the div containing the inner header div
				var headerParent = element.getElementsByTagName("div")[1];

				// find the inner header div
				var header = headerParent.getElementsByTagName("div")[0];

				// body div should be next div after header parent
				var body = headerParent.nextSibling;

				// interate until div is found
				while (body.tagName !== "DIV") {
					body = body.nextSibling;
				}

				// last check to find out if we are updating the right component
				if (body.className === "wicket-tree-table-body") {
					// get the right padding from header - we need to substract it from new width
					var padding;
					if (document.defaultView && document.defaultView.getComputedStyle) {
						padding = document.defaultView.getComputedStyle(headerParent, '').getPropertyValue("padding-right");
					} else if (headerParent.currentStyle) {
						padding = headerParent.currentStyle.paddingRight;
					}
					else {
						padding = 6;
					}

					padding = parseInt(padding, 10);

					// set the new width
					var w = (body.getElementsByTagName("div")[0].clientWidth - padding) + "px";

					if (w === (-padding) + "px") {
						// this can happen if the first row is hidden (e.g. rootless mode)
						// try to get the width from second row
						w = (body.getElementsByTagName("div")[1].clientWidth - padding) + "px";
					}

					if (w !== "0px") {
						header.style.width = w;
					}
				}
			}
			catch (ignore) {}
		}
	};

	Wicket.TreeTable.attached = {};

	Wicket.TreeTable.attachUpdate = function(treeTableId) {
		// get the object that contains ids of elements on which the update method was already attached
		var attached = Wicket.TreeTable.attached;

		// force updating the element
		Wicket.TreeTable.update(treeTableId);

		// if the update has not been attached to this tree table yet...
		if (typeof(attached[treeTableId]) === "undefined") {
			// ... attach it
			attached[treeTableId] = window.setInterval(function() {
				Wicket.TreeTable.update(treeTableId);
			}, 100);
		}
	};
})();
