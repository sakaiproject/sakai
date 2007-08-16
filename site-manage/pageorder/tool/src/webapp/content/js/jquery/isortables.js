/**
 * Interface Elements for jQuery
 * Sortables
 * 
 * http://interface.eyecon.ro
 * 
 * Copyright (c) 2006 Stefan Petre
 * Dual licensed under the MIT (MIT-LICENSE.txt) 
 * and GPL (GPL-LICENSE.txt) licenses.
 *   
 *
 */

/**
 * Allows you to resort elements within a container by dragging and dropping. Requires
 * the Draggables and Droppables plugins. The container and each item inside the container
 * must have an ID. Sortables are especially useful for lists.
 * 
 * @see Plugins/Interface/Draggable
 * @see Plugins/Interface/Droppable
 * @author Stefan Petre
 * @name Sortable
 * @cat Plugins/Interface
 * @param Hash options        A hash of options
 * @option String accept      The class name for items inside the container (mandatory)
 * @option String activeclass The class for the container when one of its items has started to move
 * @option String hoverclass  The class for the container when an acceptable item is inside it
 * @option String helperclass The helper is used to point to the place where the item will be 
 *                            moved. This is the class for the helper.
 * @option Float opacity      Opacity (between 0 and 1) of the item while being dragged
 * @option Boolean ghosting   When true, the sortable is ghosted when dragged
 * @option String tolerance   Either 'pointer', 'intersect', or 'fit'. See Droppable for more details
 * @option Boolean fit        When true, sortable must be inside the container in order to drop
 * @option Integer fx         Duration for the effect applied to the sortable
 * @option Function onchange  Callback that gets called when the sortable list changed. It takes
 *                            an array of serialized elements
 * @option Boolean floats     True if the sorted elements are floated
 * @option String containment Use 'parent' to constrain the drag to the container
 * @option String axis        Use 'horizontally' or 'vertically' to constrain dragging to an axis
 * @option String handle      The jQuery selector that indicates the draggable handle
 * @option DOMElement handle  The node that indicates the draggable handle
 * @option Function onHover   Callback that is called when an acceptable item is dragged over the
 *                            container. Gets the hovering DOMElement as a parameter
 * @option Function onOut     Callback that is called when an acceptable item leaves the container.
 *                            Gets the leaving DOMElement as a parameter
 * @option Object cursorAt    The mouse cursor will be moved to the offset on the dragged item
 *                            indicated by the object, which takes "top", "bottom", "left", and
 *                            "right" keys
 * @option Function onStart   Callback function triggered when the dragging starts
 * @option Function onStop    Callback function triggered when the dragging stops
 * @example                   $('ul').Sortable(
 *                            	{
 *                            		accept : 'sortableitem',
 *                            		activeclass : 'sortableactive',
 *                             		hoverclass : 'sortablehover',
 *                             		helperclass : 'sorthelper',
 *                             		opacity: 	0.5,
 *                             		fit :	false
 *                             	}
 *                             )
 */

jQuery.iSort = {
	changed : [],
	collected : {},
	helper : false,
	inFrontOf: null,
	
	start : function ()
	{
		if (jQuery.iDrag.dragged == null) {
			return;
		}
		var shs, margins,c, cs;
		
		jQuery.iSort.helper.get(0).className = jQuery.iDrag.dragged.dragCfg.hpc;
		shs = jQuery.iSort.helper.get(0).style;
		shs.display = 'block';
		jQuery.iSort.helper.oC = jQuery.extend(
			jQuery.iUtil.getPosition(jQuery.iSort.helper.get(0)),
			jQuery.iUtil.getSize(jQuery.iSort.helper.get(0))
		);
		
		shs.width = jQuery.iDrag.dragged.dragCfg.oC.wb + 'px';
		shs.height = jQuery.iDrag.dragged.dragCfg.oC.hb + 'px';
		//shs.cssFloat = jQuery.iDrag.dragged.dragCfg.oF;
		margins = jQuery.iUtil.getMargins(jQuery.iDrag.dragged);
		shs.marginTop = margins.t;
		shs.marginRight = margins.r;
		shs.marginBottom = margins.b;
		shs.marginLeft = margins.l;
		if (jQuery.iDrag.dragged.dragCfg.ghosting == true) {
			c = jQuery.iDrag.dragged.cloneNode(true);
			cs = c.style;
			cs.marginTop = '0px';
			cs.marginRight = '0px';
			cs.marginBottom = '0px';
			cs.marginLeft = '0px';
			cs.display = 'block';
			jQuery.iSort.helper.empty().append(c);
		}
		jQuery(jQuery.iDrag.dragged).after(jQuery.iSort.helper.get(0));
		jQuery.iDrag.dragged.style.display = 'none';
	},
	
	check : function (e)
	{
		if (!e.dragCfg.so && jQuery.iDrop.overzone.sortable) {
			if (e.dragCfg.onStop)
				e.dragCfg.onStop.apply(dragged);
			jQuery(e).css('position', e.dragCfg.initialPosition || e.dragCfg.oP);
			jQuery(e).DraggableDestroy();
			jQuery(jQuery.iDrop.overzone).SortableAddItem(e);
		}
		jQuery.iSort.helper.removeClass(e.dragCfg.hpc).html('&nbsp;');
		jQuery.iSort.inFrontOf = null;
		var shs = jQuery.iSort.helper.get(0).style;
		shs.display = 'none';
		jQuery.iSort.helper.after(e);
		if (e.dragCfg.fx > 0) {
			jQuery(e).fadeIn(e.dragCfg.fx);
		}
		jQuery('body').append(jQuery.iSort.helper.get(0));
		var ts = [];
		var fnc = false;
		for(var i=0; i<jQuery.iSort.changed.length; i++){
			var iEL = jQuery.iDrop.zones[jQuery.iSort.changed[i]].get(0);
			var id = jQuery.attr(iEL, 'id');
			var ser = jQuery.iSort.serialize(id);
			if (iEL.dropCfg.os != ser.hash) {
				iEL.dropCfg.os = ser.hash;
				if (fnc == false && iEL.dropCfg.onChange) {
					fnc = iEL.dropCfg.onChange;
				}
				ser.id = id;
				ts[ts.length] = ser;
			}
		}
		jQuery.iSort.changed = [];
		if (fnc != false && ts.length > 0) {
			fnc(ts);
		}
	},
	
	checkhover : function(e,o)
	{
		if (!jQuery.iDrag.dragged)
			return;
		var cur = false;
		var i = 0;
		if ( e.dropCfg.el.size() > 0) {
			for (i = e.dropCfg.el.size(); i >0; i--) {
				if (e.dropCfg.el.get(i-1) != jQuery.iDrag.dragged) {
					if (!e.sortCfg.floats) {
						if ( 
						(e.dropCfg.el.get(i-1).pos.y + e.dropCfg.el.get(i-1).pos.hb/2) > jQuery.iDrag.dragged.dragCfg.ny  
						) {
							cur = e.dropCfg.el.get(i-1);
						} else {
							break;
						}
					} else {
						if (
						(e.dropCfg.el.get(i-1).pos.x + e.dropCfg.el.get(i-1).pos.wb/2) > jQuery.iDrag.dragged.dragCfg.nx && 
						(e.dropCfg.el.get(i-1).pos.y + e.dropCfg.el.get(i-1).pos.hb/2) > jQuery.iDrag.dragged.dragCfg.ny  
						) {
							cur = e.dropCfg.el.get(i-1);
						}
					}
				}
			}
		}
		//helpos = jQuery.iUtil.getPos(jQuery.iSort.helper.get(0));
		if (cur && jQuery.iSort.inFrontOf != cur) {
			jQuery.iSort.inFrontOf = cur;
			jQuery(cur).before(jQuery.iSort.helper.get(0));
		} else if(!cur && (jQuery.iSort.inFrontOf != null || jQuery.iSort.helper.get(0).parentNode != e) ) {
			jQuery.iSort.inFrontOf = null;
			jQuery(e).append(jQuery.iSort.helper.get(0));
		}
		jQuery.iSort.helper.get(0).style.display = 'block';
	},
	
	measure : function (e)
	{
		if (jQuery.iDrag.dragged == null) {
			return;
		}
		e.dropCfg.el.each (
			function ()
			{
				this.pos = jQuery.extend(
					jQuery.iUtil.getSizeLite(this),
					jQuery.iUtil.getPositionLite(this)
				);
			}
		);
	},
	
	serialize : function(s)
	{
		var i;
		var h = '';
		var o = {};
		if (s) {
			if (jQuery.iSort.collected[s] ) {
				o[s] = [];
				jQuery('#' + s + ' .' + jQuery.iSort.collected[s]).each(
					function ()
					{
						if (h.length > 0) {
							h += '&';
						}
						h += s + '[]=' + jQuery.attr(this,'id');
						o[s][o[s].length] = jQuery.attr(this,'id');
					}
				);
			} else {
				for ( a in s) {
					if (jQuery.iSort.collected[s[a]] ) {
						o[s[a]] = [];			
						jQuery('#' + s[a] + ' .' + jQuery.iSort.collected[s[a]]).each(
							function ()
							{
								if (h.length > 0) {
									h += '&';
								}
								h += s[a] + '[]=' + jQuery.attr(this,'id');
								o[s[a]][o[s[a]].length] = jQuery.attr(this,'id');
							}
						);
					}
				}
			}
		} else {
			for ( i in jQuery.iSort.collected){
				o[i] = [];
				jQuery('#' + i + ' .' + jQuery.iSort.collected[i]).each(
					function ()
					{
						if (h.length > 0) {
							h += '&';
						}
						h += i + '[]=' + jQuery.attr(this,'id');
						o[i][o[i].length] = jQuery.attr(this,'id');
					}
				);
			}
		}
		return {hash:h, o:o};
	},
	
	addItem : function (e)
	{
		if ( !e.childNodes ) {
			return;
		}
		return this.each(
			function ()
			{
				if(!this.sortCfg || !jQuery(e).is('.' +  this.sortCfg.accept))
					jQuery(e).addClass(this.sortCfg.accept);
				jQuery(e).Draggable(this.sortCfg.dragCfg);
			}
		);
	},
	
	destroy: function()
	{
		return this.each(
			function()
			{
				jQuery('.' + this.sortCfg.accept).DraggableDestroy();
				jQuery(this).DroppableDestroy();
				this.sortCfg = null;
				this.isSortable = null;
			}
		);
	},
	
	build : function (o)
	{
		if (o.accept && jQuery.iUtil && jQuery.iDrag && jQuery.iDrop) {
			if (!jQuery.iSort.helper) {
				jQuery('body',document).append('<div id="sortHelper">&nbsp;</div>');
				jQuery.iSort.helper = jQuery('#sortHelper');
				jQuery.iSort.helper.get(0).style.display = 'none';
			}
			this.Droppable(
				{
					accept :  o.accept,
					activeclass : o.activeclass ? o.activeclass : false,
					hoverclass : o.hoverclass ? o.hoverclass : false,
					helperclass : o.helperclass ? o.helperclass : false,
					/*onDrop: function (drag, fx) 
							{
								jQuery.iSort.helper.after(drag);
								if (fx > 0) {
									jQuery(drag).fadeIn(fx);
								}
							},*/
					onHover: o.onHover||o.onhover,
					onOut: o.onOut||o.onout,
					sortable : true,
					onChange : 	o.onChange||o.onchange,
					fx : o.fx ? o.fx : false,
					ghosting : o.ghosting ? true : false,
					tolerance: o.tolerance ? o.tolerance : 'intersect'
				}
			);
			
			this.Keyable(
				{
					accept : o.accept,
					activeclass : o.activeclass ? o.activeclass : false,
					hoverclass : o.hoverclass ? o.hoverclass : false,
					helperclass : o.helperclass ? o.helperclass : false,
					domNode : o.domNode	? o.domNode : false						
				}
			);
			
			return this.each(
				function()
				{
					var dragCfg = {
						revert : o.revert? true : false,
						zindex : 3000,
						opacity : o.opacity ? parseFloat(o.opacity) : false,
						hpc : o.helperclass ? o.helperclass : false,
						fx : o.fx ? o.fx : false,
						so : true,
						ghosting : o.ghosting ? true : false,
						handle: o.handle ? o.handle : null,
						containment: o.containment ? o.containment : null,
						onStart : o.onStart && o.onStart.constructor == Function ? o.onStart : false,
						onDrag : o.onDrag && o.onDrag.constructor == Function ? o.onDrag : false,
						onStop : o.onStop && o.onStop.constructor == Function ? o.onStop : false,
						axis : /vertically|horizontally/.test(o.axis) ? o.axis : false,
						snapDistance : o.snapDistance ? parseInt(o.snapDistance)||0 : false,
						cursorAt: o.cursorAt ? o.cursorAt : false
					};
					jQuery('.' + o.accept, this).Draggable(dragCfg);
					this.isSortable = true;
					this.sortCfg = {
						accept :  o.accept,
						revert : o.revert? true : false,
						zindex : 3000,
						opacity : o.opacity ? parseFloat(o.opacity) : false,
						hpc : o.helperclass ? o.helperclass : false,
						fx : o.fx ? o.fx : false,
						so : true,
						ghosting : o.ghosting ? true : false,
						handle: o.handle ? o.handle : null,
						containment: o.containment ? o.containment : null,
						floats: o.floats ? true : false,
						dragCfg : dragCfg
					}
				}
			);
		}
	}
};

jQuery.fn.extend(
	{
		Sortable : jQuery.iSort.build,
		/**
		 * A new item can be added to a sortable by adding it to the DOM and then adding it via
		 * SortableAddItem. 
		 *
		 * @name SortableAddItem
		 * @param DOMElement elem A DOM Element to add to the sortable list
		 * @example $('#sortable1').append('<li id="newitem">new item</li>')
		 *                         .SortableAddItem($("#new_item")[0])
		 * @type jQuery
		 * @cat Plugins/Interface
		 */
		SortableAddItem : jQuery.iSort.addItem,
		/**
		 * Destroy a sortable
		 *
		 * @name SortableDestroy
		 * @example $('#sortable1').SortableDestroy();
		 * @type jQuery
		 * @cat Plugins/Interface
		 */
		SortableDestroy: jQuery.iSort.destroy
	}
);

/**
 * This function returns the hash and an object (can be used as arguments for $.post) for every 
 * sortable in the page or specific sortables. The hash is based on the 'id' attributes of 
 * container and items.
 *
 * @params String sortable The id of the sortable to serialize
 * @name $.SortSerialize
 * @type String
 * @cat Plugins/Interface
 */

jQuery.SortSerialize = jQuery.iSort.serialize;