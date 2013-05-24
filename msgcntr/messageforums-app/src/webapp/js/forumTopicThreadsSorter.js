/* Client side sorting for sakai forums topic threads page
 * 
 * threadsSorter:	jQuery plugin
 * @example		:	$('table').threadsSorter();
 * Version		:	1.2
 * @requires	:	tablesorter plugin
 * Author		:	Yuanhua Qu, Texas State University
 * Date			:	7/27/2010
 * Mail			:	yq12@txstate.edu
 * Description	:	This threadsSorter jquery is built on top of tablesorter jquery plguin.
 * 					It handles specifically for sakai forums topic threads sorting case. 
 * 					Should also handle sorting normal table correctly. Works tested with IE7 & IE8,
 * 					firefox 3 and safari 3 & 5 
 * 					Each thread in the topic is called -- parent
 * 					Each response directly to the thread (parent) is called -- child(ren)
 * 					Each response to the child and deeper from there is called grandchild(ren).
 * 					All responses are called descendants which should include children and grandchildren.
 * Sorting result expected:
 * 				1.	If sorted by threads, all parents are sorted; Each parent's children are also sorted;
 * 					but grandchildren are not sorted (This satisfies our users' need of no deeper sorting needed. 
 * 					Though potentially all could be sorted with same level of messages, only need little bit more
 * 					work to get it done if there is such requirement.) 
 * 					Thread/message/response still keep their logic relative layout after sorting.
 * 				2.	If sorted by date, all parents are sorted; Each parent's descendants (children and grandchildren) 
 * 					are also sorted. Descendants are not ordered by logic but date. 
 * 				3.	If sorted by author, it is absolutely sorted by author's firstname lastname order so that 
 * 					instructor will get all the messages grouped by author; Threads/messages/responses are
 * 					out of their logic relative layout after sorting. This satisfies the use case of 
 * 					instructor's insterest to see messages of certain students.
 * 					If you would like sorting by author behaves the same way as sorting by date, just comment out
 * 					3 lines:
 * 								if ($(e.currentTarget).text().toLowerCase().indexOf("author") != -1) {
 * 									return false;
 * 								}
 * Note			:	It depends on the row class, id and indent to identify parent-descendant relationship.
 * 					It is highly customized sorting result based on our users' needs.
 * 					version 1.1 fixed issue with IE browsers.
 * 					version 1.2 modified to work with sakai 2 trunk and 2.7 & 2.6 branch better with jQuery 1.1.4;
 * 							replaceWith() is only available after jQuery1.2
 */

var emCache = {};


/* This helps to convert padding value from px to relative value */	
$.fn.toEm = function(settings){
	
		
	if(!emCache[this[0]]){
		settings = jQuery.extend({
			scope: 'body'
		}, settings);
		
		var that = parseInt(this[0],10);
		var scopeTest = jQuery('<div style="display: none; font-size: 1em; margin: 0; padding:0; height: auto; line-height: 1; border:0;">&nbsp;</div>').appendTo(settings.scope);
		var scopeVal = scopeTest.height();
		scopeTest.remove();
		
		emCache[this[0]] = (that / scopeVal).toFixed(0) + 'em';
	}
	return emCache[this[0]];
};


/* add parser for link column 'Thread' , extend parser of tablesorter object */
/* 'A' fix for IE  */
$.tablesorter.addParser({
	id: "link",
	is: function(s) {
		return /^<(a|A)/.test(s);
	},
	format: function(s) {
		var title = jQuery.trim($(s).filter("a").filter(function(){return this.text!=""}).text().toLowerCase());
		if(title ==""){
			title = jQuery.trim($(s).find('a').text().toLowerCase());
		}
		return title;	
	},
	type: "text"
});

jQuery.fn.threadsSorter = function() {
	return this.each(function(){
	
		/* util */
		function isParent(node){
			if (node.className.match(new RegExp('hierItemBlock'))!= null)
				return true;
			else
				return false;
		}
		
		function isDescendant(node){
				if (node.id.match(new RegExp('_id_[0-9]+__hide_division_')) != null)
				return true;
			else 
				return false;  
		}	
		
		function isChild(node){
			var paddingValue = $(node).find("td").eq(1).css("padding-left");
			if (paddingValue.indexOf("px")>=0){
				paddingValue = $(parseInt(paddingValue.replace("px",""))).toEm();
			}
			if(paddingValue == "1em"){
					return true;
			}
			else
				return false;
		}
		
			
		/* get original table cache before it's sorted to 
		   record and mark the parent/desendant relationship in each row.
		   Need to remember row index for the children of each children node  */
		function buildOriginalCache(table) {
			var totalRows, cache;
			totalRows = (table.tBodies[0] && table.tBodies[0].rows.length) || 0,
			cache = {row:[], childrenId:[]};
			var parentId = null;
			var descendantCount;
				
			for (var i=0;i < totalRows; ++i) {
				var c = table.tBodies[0].rows[i], cols = [];
				if (isParent(c)) {
					descendantCount = 0;
					parentId = "parent" + i;
					c.parentId = "parent" + i;
					for(var k=i+1; k<totalRows; k++){
						var b = table.tBodies[0].rows[k];
						if(b.id.match(new RegExp('__hide_division_'))){
							descendantCount = descendantCount + 1;
						}
						else 
							break;
					}
					c.descendantCount = descendantCount;
				}

				if(isDescendant(c)) {
					//Remember its parent
					c.parent = parentId;
					if(isChild(c)){
						//checking siblings after it and identifying them and save row indexes if they are its children 
						var index = c.rowIndex;
						var grandChildrenRows = [];
						var grandChildrenCount = 0;
						var next = index +1;
						var tempRowIndex = i+1;   //It's the array index in originalCacheTable;
												  //Row index in the orginalCacheTable is different with table properties rowIndex
						var row = table.rows[next];
						var leftpadding;
						var paddingDigitValue;    //Relative value without 'em', ex: 2 in '2em';

						//For css style 'padding-left: 1em', firefox returns pixels like 16px or else ; safari and IE returns 1em;
						
						leftpadding = $(row).find("td").eq(1).css("padding-left");
						if(leftpadding != null){
							if(leftpadding.indexOf("px") >= 0){
								var pixels = parseInt(leftpadding.replace("px", ""));
								paddingDigitValue = parseInt($(pixels).toEm().replace("em",""));
							}
							else{
								paddingDigitValue = parseInt(leftpadding.replace("em",""));
							}
 						}						

						//  while (next ) is a grandchild, save the array index in the originalCacheTable for the row;
						while(paddingDigitValue > 1){
							grandChildrenCount++;
							grandChildrenRows.push(tempRowIndex);
							next = next + 1;
							tempRowIndex = tempRowIndex +1;
							if(next < table.rows.length){
								row = table.rows[next];
								leftpadding = $(row).find("td").eq(1).css("padding-left");
								if(leftpadding.indexOf("px") >= 0){
									var pixels = parseInt(leftpadding.replace("px", ""));
									paddingDigitValue = parseInt($(pixels).toEm().replace("em",""));
								}
								else{
									paddingDigitValue = parseInt(leftpadding.replace("em",""));
								}
							}else{
								paddingDigitValue = -1;
							}
						} //end of while loop
						c.grandChildrenCount = grandChildrenCount;
						c.grandChildrenRows = grandChildrenRows;
					}// end of isChild
				}//end of isDescendant
				cache.row.push($(c));
			}; //end of first for loop
			return cache;
		};//end of buildOriginalCache
			
		/* Record order of sorted list for children nodes */
		function buildSortedCache(table){
			var totalRows, cache;
			totalRows = (table.tBodies[0] && table.tBodies[0].rows.length) || 0,
			cache = {row:[]};
		
			for (var i=0;i < totalRows; ++i) {
				var c = table.tBodies[0].rows[i];
				var descendantRow = [];
				if (isParent(c)) {
					var parentId = c.parentId;
					var totalDescendant = c.descendantCount;
					if (totalDescendant != 0) {
						var descendant = 0;
						for(var k=0; k<totalRows; k++){
							var b = table.tBodies[0].rows[k];
							if(isDescendant(b) && b.parent == parentId){
								descendantRow.push(k);
								descendant = descendant + 1;
							}
							if(! (descendant < totalDescendant)){
								break;
							}
						}
						c.descendantRow = descendantRow;
					}
				}
				cache.row.push($(c));
			};
			return cache;
		};  //end of buildSortedCache
		
		/* 
		 * Build final sorted table for forum topic threads maintaining 
		 * parent - children - grandchildren relationship as needed
		**/
		function buildForumSortedTable(table,cache,sortedByThread,original){

			//remove all the children, then insert all the sorted children for each parent row
			$(table.tBodies[0].rows).not($("tr.hierItemBlock")).remove();

			//Treating each parent row, adding children and grandchildren
			$(table.tBodies[0].rows).each(function(i){
				if(this.descendantCount == 0){
					return true;
				}
				else{
				
					if (!sortedByThread){
						for (var m = this.descendantCount -1; m > 0 || m == 0;  m--){
							//get child row number in the sorted cache
							var childRow = this.descendantRow[m];
							//append child
							$(cache.row[childRow]).insertAfter(this);
						}
					}
					else{
						//Get children sorted, keep logic of grandchildren and deeper descendents.
						for (var m = this.descendantCount -1; m > 0 || m == 0;  m--){
							//get children row numbers in the sorted cache
							var childRow = this.descendantRow[m];
							var leftpadding;
							var paddingValue;
							//make it working cross browsers and versions hopefully, fixed IE7 & 8
							leftpadding = $(cache.row[childRow][0].children[1]).css("padding-left");
							if(leftpadding.indexOf("px")>=0){
								var pixels = leftpadding.replace("px","");
								paddingValue = $(parseInt(pixels)).toEm();
							}
							else{
								   paddingValue = leftpadding;	
							}
							if(paddingValue == "1em"){
								// Insert child
								var insertedRow = $(cache.row[childRow]).insertAfter(this);
								// Find its grandchildren and insert them in the original order 
								var count = cache.row[childRow][0].grandChildrenCount;
								if(count != 0 ){
									var rowIndexArray = cache.row[childRow][0].grandChildrenRows;
									for (var n = 0; n < count; n++){
										insertedRow = $(original.row[rowIndexArray[n]]).insertAfter(insertedRow);
									}
								}
								
							}
						}//end of for	
					}
				}
			});
		}// end of buildForumSortedTable

		/* build original table cache to mark parent-child relationship */
		cacheOriginalTable = buildOriginalCache(this);
		
		/* Calling jquery library tablesorter plugin function to do general sorting */
		/* disable sorting on first column */
		/* Classnames for asc and desc in jquery.tablesorter.js seem defined in a opposite way.*/
		/* Pass in the classnames to make sorting correct. */
		
		$(this).tablesorter({
			headers:{
				0:{ sorter: false}
			},
			cssAsc:"headerSortDown",
			cssDesc:"headerSortUp"
		});

		//Showing headers clickable and sortable like sakai style
		//decorateHeaders();
		
		$this = $(this);

		/* 
		 * add another click handler doing customized sorting for forum topic threads table 
		 * except first column
		 */
		
		$(this).find("th:gt(0)").click(function(e){
		
			//IE supports srcElement, not currentTarget
			if(!e.currentTarget)
					e.currentTarget = e.srcElement;	

			//If sorted by Author, sort all authors regardless of parent/descendent relationship.

			//Comment out following 3 lines if you would like to keep parent/descendent relationship
			//and keep Author sorted within each level.

			if ($(e.currentTarget).text().toLowerCase().indexOf("author") != -1) {
				return false;
			}
			
			//Sort and keep parent/descendent relationship after sorting
			//Set timer to be 10 ms to be executed later than executing functions in tablesorter; this
			//fixes latency issue with IE browser.bugid:3565
			setTimeout(function() {			
				//build cache for normally sorted table
				cacheSortedTable = buildSortedCache($this[0]);
				//build forum special sorted table
				sortedByThread = ($(e.currentTarget).get(0).cellIndex) ==1;
				buildForumSortedTable($this[0],cacheSortedTable,sortedByThread,cacheOriginalTable);
			},3);
			return false;
		});	
		
		
		/* 
		 * The sakai expand/collapse will reload the tables which wipe out the sorted rows when expending/collapsing.
		 * We added handler for expand/collapse when clicking on first column header  -- the expand/collapse icon 
		 * to overwrite the out of box behavior, so that the table still remains sorted and sorting direction indicator
		 * still shows up while it's expanding/collapsing.
		 */
		var imageCollapseExpandUrl = "../../images/collapse-expand.gif";
		var imageExpandCollapseUrl = "../../images/expand-collapse.gif";		
		var imageCollapseUrl = "../../images/collapse.gif";
		var imageExpandUrl = "../../images/expand.gif";
	
		var expandCollapseCol = $this[0].tHead.rows[0].cells[0];
		
		//replaceWith added in jQuery 1.2, not in 1.1.4
		//$(expandCollapseCol).find("a").replaceWith("<img src=" + imageCollapseUrl + " alt='Expand All/Collapse All' title='Expand All/Collapse All'/>");
		$(expandCollapseCol).find("a").remove();
		$(expandCollapseCol).append("<img src=" + imageExpandCollapseUrl + " alt='Expand All/Collapse All' title='Expand All/Collapse All'/>");
		$(expandCollapseCol).css("cursor", "pointer");
		var flip = 0;   //indicates click times for expand all/collapse all

		$(this).find("th:eq(0)").click(function(e){
			flip++;
			var imageObj = $(e.target).is("img")?$(e.target):$(e.target).find("img");
			
			if(flip %2 == 0){
				$($this[0].tBodies[0].rows).not(".hierItemBlock").hide();
				//Sync icons showing consitent for collapsing 
				imageObj.attr({'src': imageExpandCollapseUrl, 'alt':'Expand/Collapse', 'title':'Expand/Collapse'});
				$("tr.hierItemBlock td:first-child img").attr({'src': imageCollapseUrl, 'alt':'Expand/Collapse', 'title':'Expand/Collapse'})
			}
			else {
				$($this[0].tBodies[0].rows).not(".hierItemBlock").show();
				//Sync icons showing consitent for expanding 
				imageObj.attr({'src': imageCollapseExpandUrl, 'alt':'Expand/Collapse', 'title':'Expand/Collapse'});
				$("tr.hierItemBlock td:first-child img").attr({'src': imageExpandUrl, 'alt':'Expand/Collapse', 'title':'Expand/Collapse'});
			}
			mySetMainFrameHeight($('iframe',parent.document).filter('iframe.portletMainIframe')[0].id);
			return false;
		
		});
		
 });

}; //end of jquery threadsSorter plugin 
