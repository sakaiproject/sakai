<f:view>
   <sakai:view title="#{msgs['custom.chatroom']}">

<sakai:script contextBase="/library" path="/js/jquery-latest.min.js" />
<sakai:script contextBase="/sakai-chat-tool" path="/js/ba-debug.min.js"/>
<sakai:script contextBase="/sakai-chat-tool" path="/js/jquery.ba-bbq.min.js"/>
<sakai:script contextBase="/sakai-chat-tool" path="/js/moment.min.js"/>
<style type="text/css">
		tr.one {background-color: #dcdcdc;}
		tr.two {background-color: #fcfcfc;}
</style>

<script>
	//Converts an array of arrays to a super simple table. First row is header
	function toTable(data,id) {
			var out="<table>";
					$.each(data, function() {
							out+="<tr>";
									for (item in this) {
											out+="<td>"+this[item]+"</td>";
									};
									out+="</tr>";
					});
					out+="</table>"
			return out;
	}

	//Display all chat messages in a nice table
	//http://localhost:8080/direct/chat-message.json?channelId=ad9e8584-ffa6-4f63-b48a-26a3c7428224
  $(document).ready(function() {
		var myObj = $.deparam.querystring();
		//Get channel id from parameters
		var channelId = myObj["channelId"];
		//Loop through all messages in this channel
		var rows=new Array();
		if (channelId != null) {
				$.ajax({ url: '/direct/chat-message.json', dataType: 'json', async: false, data: "channelId="+channelId, success: function(data) {
								//Push the header
								rows.push(["Date","Author","Message"]);
								$.each(data["chat-message_collection"], function(key,val) {
										var messageURL= val["entityURL"]+".json";
										if (messageURL) {
												$.ajax({url:messageURL,dataType:'json',async:false,success:function(data2) {
																row = [moment(data2["messageDate"]).format("YYYY-MM-dd HH:mm:ssZ"), data2["ownerDisplayName"], data2["body"]];
																rows.push(row);
														}
												});
										}
								});	
						}
				});
		}
		debug.log(rows);
		$("#div1content").html(toTable(rows,"table1"));
		//Stripe the content
		$("tr:nth-child(odd)").addClass("two");
		$("tr:nth-child(even)").addClass("one");	
		});
</script>
<div id="div1content"></div>
</sakai:view>
</f:view>
