timeout=null;
		timeoutDelay=400;
		$(document).ready(
			function() {
				$("img.infoDiv").hover(
					function() { 
						that=this; 
						timeout = setTimeout(function(){ 
									$(that).next("table:eq(0)").css("display","inline");} , timeoutDelay);
					}, 
					function() { 
						clearTimeout(timeout);
						$(that).next("table:eq(0)").css("display","none");
				});	
				
		});	