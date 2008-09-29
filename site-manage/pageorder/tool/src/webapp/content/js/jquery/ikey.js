/*
 Fluid Project

 Copyright (c) 2006, 2007 University of Toronto. All rights reserved.

 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

       http://www.osedu.org/licenses/ECL-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Adaptive Technology Resource Centre, University of Toronto
 130 St. George St., Toronto, Ontario, Canada
 Telephone: (416) 978-4360
*/

/**
 * Allows you to resort elements within a sortable container by using the keyboard. Requires
 * the Draggables, Droppables and Sortables interface plugins. The container and each item inside 
 * the container must have an ID. Sortables are especially useful for lists.
 * 
 * @see Plugins/Interface/Draggable
 * @see Plugins/Interface/Droppable
 * @see Plugins/Interface/Sortable
 * @author Joshua Ryan
 * @author Colin Clark
 * @name Keyable
 * @cat Plugins/Interface
 * @option String accept      The class name for items inside the container (mandatory)
 * @option String activeclass The class for the container when one of its items has started to move
 * @option String hoverclass  The class for the container when an acceptable item is inside it
 * @option String helperclass The helper is used to point to the place where the item will be 
 *                            moved. This is the class for the helper.
 * @option Function onChange  Callback that gets called when the sortable list changed. It takes
 *                            an array of serialized elements
 * @option String axis        Use 'horizontally' or 'vertically' to constrain dragging to an axis
 * @option DOMElement domNode The conatainer of keyable items
 * @option Function onStart   Callback function triggered when the dragging starts
 * @option Function onStop    Callback function triggered when the dragging stops
 * @example                   $('ul').Keyable(
 *                            	{
 *                            		accept : 'sortableitem',
 *                            		activeclass : 'sortableactive',
 *                             		hoverclass : 'sortablehover',
 *                             		helperclass : 'sorthelper',
 *                                      domNode : $('ul').get(0)
 *                             	}
 *                             )
 */

jQuery.iKey = {

	// The node focused on for incoming actions
	focusedNode : null,
	
	// Sets the mode of keying vs mousing
	keying : false,

	/**
	 * Process down arrow events
	 */
	handleDownAction : function (isCtrl, event) {
		var target = jQuery(jQuery.iKey.focusedNode).next();
		var wrap;
		
		if (!target || !this.isElement(target.get(0))) {
			target = jQuery(jQuery.iKey.firstElement(
				jQuery.iKey.focusedNode.get(0).parentNode)
			);
			wrap = true;
		}
		
		if (!isCtrl) {
			this.focusNode(target, event);
		}
		else if (!wrap) {
			jQuery(target).after(jQuery.iKey.focusedNode);
		}
		else {
			jQuery(target).before(jQuery.iKey.focusedNode);
		}
	},

	/**
	 * Process up arrow events
	 */
	handleUpAction : function(isCtrl, event) {
		var target = jQuery(jQuery.iKey.focusedNode).prev();
		var wrap = false;
		
		if (!target || !this.isElement(target.get(0))) {
			target = jQuery(jQuery.iKey.lastElement(
				jQuery.iKey.focusedNode.get(0).parentNode)
			);
			wrap = true;
		}
		
		if (!isCtrl) {
			this.focusNode(target, event);
		}
		else if (!wrap) {
			jQuery(target).before(jQuery.iKey.focusedNode);
		}
		else {
			jQuery(target).after(jQuery.iKey.focusedNode);
		}

	},

	/**
	 * 'Focus' on a node to be the focus of future actions 
	 */
	focusNode : function(aNode, event) {			
		// deselect any previously focused node
		jQuery.iKey.deselectFocusedNode(event);
					
		jQuery.iKey.focusedNode = aNode;			
		
		jQuery(aNode).removeClass(event.data.accept);
		jQuery(aNode).addClass(event.data.activeclass);
	},

	/**
	 * 'Select' the focused node, similar to a user 'clicking' on an item for drag and drop
	 */
	selectFocusedNode : function(event) {
		//if we are not in keyboard sort mode, set things up
		if (jQuery.iKey.focusedNode == null) {
			jQuery.iKey.focusedNode = jQuery('.' + event.data.accept, event.data.domNode).get(0);
		}
		if (jQuery.iKey.keying == true) {
			jQuery.iKey.focusNode(jQuery.iKey.focusedNode, event);
		}
	},

	/**
	 * Deselect the current selected node, similar to releasing the mouse button
	 */
	deselectFocusedNode : function(event) {
		if (jQuery.iKey.focusedNode != null) {
			jQuery(jQuery.iKey.focusedNode).removeClass(event.data.activeclass);
			jQuery(jQuery.iKey.focusedNode).removeClass(event.data.hoverclass);
			jQuery(jQuery.iKey.focusedNode).addClass(event.data.accept);
			jQuery.iKey.focusedNode = null;
		}
	},

	/**
	 * End keyboard mode, for use when users switches to using the mouse for DnD type activities
	 */
	endKeyboardMode : function(event) {
		if (jQuery.iKey.keying) {
			jQuery.iKey.deselectFocusedNode(event);
			jQuery(document)
				.unbind('mousemove', jQuery.iKey.endKeyboardMode)
				.unbind('mousedown', jQuery.iKey.endKeyboardMode);
		}
		jQuery.iKey.keying = false;
	},

	/**
	 * Change state from that of selecting a node to being ready to actually move the current node
	 */	
	handleKeyDown : function (event) {
		if (event.ctrlKey && jQuery.iKey.focusedNode != null) {
			jQuery(jQuery.iKey.focusedNode).removeClass(event.data.activeclass);
			jQuery(jQuery.iKey.focusedNode).addClass(event.data.hoverclass);
		}
	},
	
	/**
	 * Change state from that of being ready to move a node to that of selecting a node from the list
	 */
	handleKeyUp : function (event) {
		kCode = event.keyCode || event.which;
		if (kCode == 17 && jQuery.iKey.focusedNode != null) {
			jQuery(jQuery.iKey.focusedNode).removeClass(event.data.hoverclass);
			jQuery(jQuery.iKey.focusedNode).addClass(event.data.activeclass);
		}
	},
	
	/**
	 * Handle arrow key presses, could be either moving through the list to select a node or moving a node
	 */
	handleArrowKeyPress : function (event) {
		kCode = event.keyCode || event.which;

		// Pass any input other then arrow keys onto other event handlers
		if (kCode < 37 || kCode > 40) {
			return true;
		}

		// Listen for mouse actions to end keyboard mode
		if (!jQuery.iKey.keying) {
			jQuery.iKey.keying = true;
			jQuery(document)
				.bind('mousemove', event.data, jQuery.iKey.endKeyboardMode)
				.bind('mousedown', event.data, jQuery.iKey.endKeyboardMode);	
		}
	
		// Ensure a focused node
		while (!jQuery.iKey.focusedNode) {
			jQuery.iKey.selectFocusedNode(event);
		}

		// down arrow
		if (kCode == 40 && (!event.data.axis || event.data.axis == 'vertically')) {
			jQuery.iKey.handleDownAction(event.ctrlKey, event);								
		}
		// up arrow
		else if (kCode == 38 && (!event.data.axis || event.data.axis == 'vertically')) {
			jQuery.iKey.handleUpAction(event.ctrlKey, event);
		}
		// right arrow
		else if (kCode == 39 && (event.data.axis || event.data.axis == 'horizontally')) {
			jQuery.iKey.handleDownAction(event.ctrlKey, event);								
		}
		// left arrow
		else if (kCode == 37 && (event.data.axis || event.data.axis == 'horizontally')) {
			jQuery.iKey.handleUpAction(event.ctrlKey, event);
		}
		else {
			return true;
		}
		return false;
	},

	/**
	 * Gets the first Element of a nodes child node list
	 */
	firstElement : function(node) {
		var child = node.firstChild;
		
		while (!this.isElement(child)) {
			return child = child.nextSibling;
		}
		return child;
	},
	
	/**
	 * Gets the last Element of a nodes child node list
	 */
	lastElement : function(node) {
		var child = node.lastChild;
		
		while (!this.isElement(child)) {
			child = child.previousSibling;
		}
		return child;
	},
	
	/**
	 * tests if the passed in node is an Element
	 */
	isElement : function(node) {
		return node && node.nodeType == 1;
	},
	
	/**
	 * Builds the Keyable with the set parameters and binds all neeeded events.
	 *
	 * Gets called when ever a Keyable is created.
	 */
	build : function(o) {
		if (!o) {
			o = {};
		}
		return this.each(
			function() {
				if (this.isKeyable || !jQuery.iUtil) {
					return;
				}
				var el = this;
				var dhe = jQuery(this);

				if (jQuery.browser.msie) {
					dhe.each(
						function() {
							this.unselectable = "on";
						}
					);
				}
				else {
					dhe.css('-moz-user-select', 'none');
					dhe.css('user-select', 'none');
					dhe.css('-khtml-user-select', 'none');
				}
				
				this.keyCfg = {
					domNode :     o.domNode ? o.domNode : false,
					accept :      o.accept || false,
					activeclass : o.activeclass || false, 
					hoverclass :  o.hoverclass || false,
					helperclass : o.helperclass || false,
					axis :        /vertically|horizontally/.test(o.axis) ? o.axis : false,
					onStart :     o.onStart || o.onstart || false,
					onStop :      o.onStop || o.onstop || false					
				};
				
				dhe.each(
					function() {
						jQuery(this).bind('keypress', el.keyCfg, jQuery.iKey.handleArrowKeyPress);
						jQuery(this).bind('keydown', el.keyCfg, jQuery.iKey.handleKeyDown);
						jQuery(this).bind('keyup', el.keyCfg, jQuery.iKey.handleKeyUp);
						jQuery(this).bind('onfocus', el.keyCfg, jQuery.iKey.selectFocusedNode);
						jQuery(this).bind('focus', el.keyCfg, jQuery.iKey.selectFocusedNode);
						jQuery(this).bind('onblur', el.keyCfg, jQuery.iKey.endKeyboardMode);
						jQuery(this).bind('onclick', el.keyCfg, jQuery.iKey.endKeyboardMode);
						jQuery(this).bind('onmousedown', el.keyCfg, jQuery.iKey.endKeyboardMode);
						jQuery(this).bind('onmousemove', el.keyCfg, jQuery.iKey.endKeyboardMode);
					}
				);
			}
		);
	}	
};

/**
 * Destroy an existing draggable on a collection of elements
 * 
 * @name DraggableDestroy
 * @descr Destroy a draggable
 * @type jQuery
 * @cat Plugins/Interface
 * @example $('#drag2').DraggableDestroy();
 */

jQuery.fn.extend(
	{
		KeyableDestroy : jQuery.iKey.destroy,
		Keyable : jQuery.iKey.build
	}
);
