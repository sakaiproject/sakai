/**
 * Copyright 2015, Zap Lin
 * All rights reserved.
 *
 * This source code is licensed under the Apache license found in the
 * LICENSE file in the root directory of this source tree.
 */

(function($){

	// waterfall option of every elements
	var g_option = {
		// default setting 
		_init_:{
			top : false,	// all of column height
			w : false,		// current container-width
			col : false,	// grid number 
			gap : 10,
			gridWidth : [0,400,600,800,1200],
			refresh: 500,
			timer : false,
			scrollbottom : false
		}
	},
	// container id
	hash = 0;

	// waterfall methods (can be called)
	var methods = {
		init : function() {
			// first call, set default

			var id = getHashId(this);

			if(!g_option[id]){
				g_option[id]=$.extend({},g_option._init_);
			}

			// overwrite setting
			if(arguments[0]){
				g_option[id]=$.extend(g_option[id],arguments[0]);
			}
	
			// if set scroll to bottom, overwrite setting`
			if(g_option[id].scrollbottom){
				g_option[id].scrollbottom = $.extend({
					ele : this.parent(),
					endele: $('<div>').css({width:'100%',textAlign:'center',position:'absolute'}),
					endtxt: 'No More Data',
					gap: 300
				},g_option[id].scrollbottom);
			}

			// Waterfall is a absolute-position base layout
			// the container must be relative or absolute position
			// This setting needs to be strengthened
			this.css('position', 'relative');
			// start
			detect(this);
	  	},

		sort : function() { 
			sorting(this); 
		},
		stop : function() {
			var id = getHashId(this);
			if(g_option[id].timer){ 
				clearInterval(g_option[id].timer); 
				g_option[id].timer = false; 
			}
		},
		end : function() {
			var id = getHashId(this);
			if(g_option[id].scrollbottom){
				g_option[id].scrollbottom.ele.css('top',g_option[id].top[getPolesCol(id,true)]+"px");
				this.append(g_option[id].scrollbottom.endele);
			}
			if(g_option[id].timer){ 
				clearInterval(g_option[id].timer); 
				g_option[id].timer = false; 
			}		

		}
	};

	/*
		get min or max col
		@param id : hash id
		@param boo: true is max ,else min
	*/
	function getPolesCol(id,boo){
		var top = g_option[id].top, col = 0, v =top[col];
		for(var i=0;i<top.length;i++) {
			if(boo){
				if(top[i]>v){ 
					v = top[i]; 
					col = i;
				}	
			}
			else{
				if(top[i]<v){ 
					v = top[i]; 
					col = i;
				}
			}
		}
		return col;
	}

	function sorting(t){
		var id = getHashId(t);
		var gw = g_option[id].gridWidth,
			w = g_option[id].w,
			gap =g_option[id].gap,
			scrollbottom = g_option[id].scrollbottom;
		
		g_option[id].col = 1;
		g_option[id].top=[];

		for(var i=gw.length-1;i>=0;i--){
			if(w>gw[i]){ 
				g_option[id].col = i+1; break; 
			}
		}

		var cwidth =(w-((g_option[id].col-1)*gap))/g_option[id].col,
			left=[];

		for(var j=0;j<g_option[id].col;j++){
			left.push(j*cwidth+j*gap);
			g_option[id].top.push(0);
		}
		t.children().css({
			position:'absolute',
			left: (w/2-cwidth/2)+'px',
			top: t.scrollTop(),
			transition: 'left ' + g_option[id].refresh + 'ms ease-in-out,' +
						'top ' + g_option[id].refresh + 'ms ease-in-out,' +
						'opacity ' + g_option[id].refresh + 'ms ease-in-out'
			}).each(function() {
				var ic = getPolesCol(id,false);
				$(this).css({ width: cwidth+'px', left: left[ic]+'px', top : g_option[id].top[ic]+'px',opacity:'1' });
				g_option[id].top[ic]+=$(this)[0].offsetHeight+gap;
			});
			//set the waterfall box height
            t.css("height",g_option[id].top[getPolesCol(id,true)] + "px");
			if(scrollbottom)
				if(scrollbottom.endele)
					scrollbottom.endele
						.addClass('endele')
						.text(scrollbottom.endtxt)
						.css('top', g_option[id].top[getPolesCol(id,true)]+"px");
	}

	// detect screen width change , resort cards
	function detect(t){
		var id = getHashId(t);
		if(!g_option[id].timer){
			g_option[id].timer =  setInterval(function(){
				var bw = t[0].offsetWidth;
				if(g_option[id].w!==bw) {
					g_option[id].w=bw;  
					sorting(t); 
				}
				if(g_option[id].scrollbottom){
					if(g_option[id].scrollbottom.callback && isbottom(g_option[id].scrollbottom.ele,g_option[id].scrollbottom.gap)){
						g_option[id].scrollbottom.callback(t);
					}
				}
			},g_option[id].refresh);
		}
		sorting(t);
	}


	// return is scroll to bottom  
	function isbottom(ele,gap){
		var wh = $(window).height();
		return ((wh+ele.scrollTop())>(ele.prop("scrollHeight")-gap));
	}


	// get element unique id
	function getHashId(t){
		if(!t.attr('wf-id')){
			hash+=0.1;
			t.attr('wf-id',hash);
		}
		return t.attr('wf-id');
	}

	$.fn.waterfall = function() {
		var res;
		if(!arguments[0] || typeof arguments[0] === 'object'){
			res = methods.init.apply(this,arguments);
		}
		else if(methods[arguments[0]]){
			res = methods[ arguments[0] ].apply( this, Array.prototype.slice.call( arguments[0], 1 ));
		}
		else {
			$.error( 'Method ' +  arguments[0] + ' does not exist on jQuery.waterfall' );
		}
		return res || this;
	};

})(jQuery);

