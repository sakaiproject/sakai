/**
 * jqBarGraph - jQuery plugin
 * @version: 1.1 (2011/04/03)
 * @requires jQuery v1.2.2 or later 
 * @author Ivan Lazarevic
 * Examples and documentation at: http://www.workshop.rs/jqbargraph/
 * 
 * Dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 * 
 * @param data: arrayOfData // array of data for your graph
 * @param title: false // title of your graph, accept HTML
 * @param barSpace: 10 // this is default space between bars in pixels
 * @param width: 400 // default width of your graph ghhjgjhg
 * @param height: 200 //default height of your graph
 * @param color: '#000000' // if you don't send colors for your data this will be default bars color
 * @param colors: false // array of colors that will be used for your bars and legends
 * @param lbl: '' // if there is no label in your array
 * @param sort: false // sort your data before displaying graph, you can sort as 'asc' or 'desc'
 * @param position: 'bottom' // position of your bars, can be 'bottom' or 'top'. 'top' doesn't work for multi type
 * @param prefix: '' // text that will be shown before every label
 * @param postfix: '' // text that will be shown after every label
 * @param animate: true // if you don't need animated appereance change to false
 * @param speed: 2 // speed of animation in seconds
 * @param legendWidth: 100 // width of your legend box
 * @param legend: false // if you want legend change to true
 * @param legends: false // array for legend. for simple graph type legend will be extracted from labels if you don't set this
 * @param type: false // for multi array data default graph type is stacked, you can change to 'multi' for multi bar type
 * @param showValues: true // you can use this for multi and stacked type and it will show values of every bar part
 * @param showValuesColor: '#fff' // color of font for values 

 * @example  $('#divForGraph').jqBarGraph({ data: arrayOfData });  
  
**/
// Customized to fix a small bug in a for..in loop over an array, where it would also loop over other structures
// in the array, such as "contains"

// Customized to add legends to data array and legendsAtTitle to display legends as title in the div

(function($) {
	var opts = new Array;
	var level = new Array;
	
	$.fn.jqBarGraph = $.fn.jqbargraph = function(options){
	
	init = function(el){

		opts[el.id] = $.extend({}, $.fn.jqBarGraph.defaults, options);
		$(el).css({ 'width': opts[el.id].width, 'position':'relative', 'text-align':'center', 'display':'flex', 'align-items':'flex-end' });
		doGraph(el);

	};
	
	// sum of array elements
	sum = function(ar){
		total = 0;
		for(val in ar){
			total += ar[val];
		}
		return total.toFixed(2);
	};
	
	// count max value of array
	max = function(ar){
		maxvalue = 0;
		for(var val in ar){
			value = ar[val][0];
			if(value instanceof Array) value = sum(value);	
			if (parseFloat(value) > parseFloat(maxvalue)) maxvalue=value;
		}	
		return maxvalue;	
	};

	// max value of multi array
	maxMulti = function(ar){
		maxvalue = 0;
		maxvalue2 = 0;
		
		for(var val in ar){
			ar2 = ar[val][0];
			
			for(var val2 in ar2){
				if(ar2[val2]>maxvalue2) maxvalue2 = ar2[val2];
			}

			if (maxvalue2>maxvalue) maxvalue=maxvalue2;
		}	
		return maxvalue;		
	};
	
		
	doGraph = function(el){
		
		arr = opts[el.id];
		data = arr.data;
		
		//check if array is bad or empty
		if(data == undefined) {
			$(el).html('There is not enought data for graph');
			return;
		}

		//sorting ascending or descending
		if(arr.sort == 'asc') data.sort(sortNumberAsc);
		if(arr.sort == 'desc') data.sort(sortNumberDesc);
		
		legend = '';
		prefix = arr.prefix;
		postfix = arr.postfix;
		space = arr.barSpace; //space between bars
		legendWidth = arr.legend ? arr.legendWidth : 0; //width of legend box
		fieldWidth = ($(el).width()-legendWidth)/data.length; //width of bar
		totalHeight =  opts[el.id].height;
		var leg = new Array(); //legends array
		
		//max value in data, I use this to calculate height of bar
		max = max(data);
		colPosition = 0; // for printing colors on simple bar graph

 		for(var val = 0; val < data.length; val++){
 			
 			valueData = data[val][0];
 			if (valueData instanceof Array) 
 				value = sum(valueData);
 			else
 				value = valueData;
 			
 			lbl = data[val][1];
 			color = data[val][2];
 			legendLbl = data[val][3];
 			unique = val+el.id; //unique identifier
 			
 			if (legendLbl == undefined && arr.legends !== undefined) {
 				legendLbl = legends[val];
 			}
 			if (color == undefined && arr.colors == false) 
 				color = arr.color;
 				
 			if (arr.colors && !color){
 				colorsCounter = arr.colors.length;
 				if (colorsCounter == colPosition) colPosition = 0;
 				color = arr.colors[colPosition];
 				colPosition++;
 			}
 			
 			if(arr.type == 'multi') color = 'none';
 				
 			if (lbl == undefined) lbl = arr.lbl;
 		
 			out  = "<div class='graphField"+el.id+"' id='graphField"+unique+"' class='jqGraphField'>";
 			out += "<div class='graphValue"+el.id+"' id='graphValue"+unique+"'>"+prefix+value+postfix+"</div>";
 			
 			out += "<div class='graphBar"+el.id+"' id='graphFieldBar"+unique+"' style='background-color:"+color+";position: relative; overflow: hidden;'></div>";

			// if there is no legend or exist legends display lbl at the bottom
 			if(!arr.legend || arr.legends)
 				out += "<div class='graphLabel"+el.id+"' id='graphLabel"+unique+"' style='white-space:nowrap;overflow:hidden;text-overflow:clip' title='"+legendLbl+"'>"+lbl+"</div>";
 			out += "</div>";
 			
			$(el).append(out);
 			
 			//size of bar
 			totalHeightBar = totalHeight;
 			fieldHeight = (totalHeightBar*value)/max;	
 			$('#graphField'+unique).css({ 
 				'flex': '0 1 ' + (fieldWidth-space) + 'px', 
				'max-width': (100.0 / data.length) + '%',
 				'margin-left': space});
 	
 			// multi array
 			if(valueData instanceof Array){
 				
				if(arr.type=="multi"){
					maxe = maxMulti(data);
					totalHeightBar = fieldHeight = totalHeight;
					$('.graphValue'+el.id).remove();
				} else {
					maxe = max;
				}
				
 				for (i in valueData){
 					heig = totalHeightBar*valueData[i]/maxe;
 					wid = parseInt((fieldWidth-space)/valueData.length);
 					sv = ''; // show values
 					fs = 0; // font size
 					if (arr.showValues){
 						sv = arr.prefix+valueData[i]+arr.postfix;
 						fs = 12; // font-size is 0 if showValues = false
 					}
 					o = "<div class='subBars"+el.id+"' style='height:"+heig+"px; background-color: "+arr.colors[i]+"; left:"+wid*i+"px; color:"+arr.showValuesColor+"; font-size:"+fs+"px' >"+sv+"</div>";
 					$('#graphFieldBar'+unique).prepend(o);
 				}
 			}
 			
 			if(arr.type=='multi')
 				$('.subBars'+el.id).css({ 'width': wid, 'position': 'absolute', 'bottom': 0 });
 
 			//position of bars
 			if(arr.position == 'bottom') $('.graphField'+el.id).css('bottom',0);

			//creating legend array from lbl if there is no legends param
 			if(!arr.legends)
 				leg.push([ color, legendLbl, el.id, unique ]); 
 			
 			// animated apearing
 			if(arr.animate){
 				$('#graphFieldBar'+unique).css({ 'height' : 0 });
 				$('#graphFieldBar'+unique).animate({'height': fieldHeight},arr.speed*1000);
 			} else {
 				$('#graphFieldBar'+unique).css({'height': fieldHeight});
 			}
 			
 		}
 			
 		//creating legend array from legends param
 		for(var l in arr.legends){
 			leg.push([ arr.colors[l], arr.legends[l], el.id, l ]);
 		}
 		
 		createLegend(leg); // create legend from array
 		
 		//position of legend
 		if(arr.legend){
			$(el).append("<div id='legendHolder"+unique+"'></div>");
	 		$('#legendHolder'+unique).css({ 'width': legendWidth, 'float': 'right', 'text-align' : 'left'});
	 		$('#legendHolder'+unique).append(legend);
	 		$('.legendBar'+el.id).css({ 'float':'left', 'margin': 3, 'height': 12, 'width': 20, 'font-size': 0});
 		}
 		
 		//position of title
 		if(arr.title){
 			$(el).wrap("<div id='graphHolder"+unique+"'></div>");
 			$('#graphHolder'+unique).prepend(arr.title).css({ 'width' : arr.width+'px', 'text-align' : 'center' });
 		}
 		
	};


	//creating legend from array
	createLegend = function(legendArr){
		legend = '';
		for(var val in legendArr){
	 			legend += "<div id='legend"+legendArr[val][3]+"' style='overflow: hidden; zoom: 1;'>";
	 			legend += "<div class='legendBar"+legendArr[val][2]+"' id='legendColor"+legendArr[val][3]+"' style='background-color:"+legendArr[val][0]+"'></div>";
	 			legend += "<div class='legendLabel"+legendArr[val][2]+"' id='graphLabel"+unique+"'>"+legendArr[val][1]+"</div>";
	 			legend += "</div>";			
		}
	};


	this.each (
		function()
		{ init(this); }
	)
	
};

	// default values
	$.fn.jqBarGraph.defaults = {	
		barSpace: 10,
		width: 400,
		height: 300,
		color: '#000000',
		colors: false,
		lbl: '',
		sort: false, // 'asc' or 'desc'
		position: 'bottom', // or 'top' doesn't work for multi type
		prefix: '',
		postfix: '',
		animate: true,
		speed: 1.5,
		legendWidth: 100,
		legend: false,
		legends: false,
		type: false, // or 'multi'
		showValues: true,
		showValuesColor: '#fff',
		title: false
	};
	
	
	//sorting functions
	function sortNumberAsc(a,b){
		if (a[0]<b[0]) return -1;
		if (a[0]>b[0]) return 1;
		return 0;
	}
	
	function sortNumberDesc(a,b){
		if (a[0]>b[0]) return -1;
		if (a[0]<b[0]) return 1;
		return 0;
	}	

})(jQuery);