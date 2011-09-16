$(document).ready(function(){
    jQuery.ajax({
        url: 'data.json',
        dataType: 'json',
        type: 'post',
        cache: false,
        contentType: 'application/json',
        
        success: function(json){
            delimitLeft="{";
            delimitRight="}"
            var results = '<div class=\"results\">';
            if (json.order.length !== 0) {
            
                $(json.order).each(function(i){
                    var o = this;
                    var w = o.toString();
                    
                    if (get_type(json[w]) === "String") {
                        // a string
                        if (json[w].split(delimitLeft).length - 1 > 0) {
                            // a string that has substitions, replace them
                            var endString = json[w];
                            var arr = json[w].split(delimitRight);
                            for (i = 0; i < arr.length; i++) {
                                var arr2 = arr[i].split(delimitLeft);
                                if (arr2[1]) {
                                    endString = endString.replace(delimitLeft + arr2[1] + delimitRight, json[arr2[1]]);
                                }
                            }
                            // should do a check here, to make sure that all the keys had a value
                            // increase a counter for each successful arr2[1] nad compare in the end with
                            // the length of json[w].split('{').length - 1
                            results = results + '<div class="metadataLine">' + endString + '</div>'
                        }
                        else 
                            if (json[w + '-label']) {
                                // a string with a label counterpart
                                results = results + '<h5>' + json[w + '-label'] + '</h5><div class="block">' + json[w] + '</div>'
                                
                            }
                            else {
                                if (w === 'title') {
                                    // a title string
                                    results = results + '<h4>' + json[w] + '</h4>'
                                }
                                else {
                                    //all other strings
                                    results = results + '<div class="block">' + json[w] + '</div>'
                                }
                            }
                        
                        
                    }
                    else {
                        // is an object, treat special
                        if (w === 'attachments' && json[w]) {
                            var atts = "";
                            for (i = 0; i < json[w].length; i++) {
                            
                                atts = atts + '<li><a href=\"' + json[w][i]['attachment-url'] + '\">' + json[w][i]['attachment-title'] + '</a></li>';
                            }
                            results = results + '<ul class=\"attachList\">' + atts + '</ul>';
                        }
                        if (w === 'more-info' && json[w]) {
                            var moreinfo = "";
                            for (i = 0; i < json['more-info'].length; i++) {
                                var target = "";
                                var size = "";
                                if (json['more-info'][i]['info_link-target']) {
                                    target = 'target=\"' + json['more-info'][i]['info_link-target'] + '\"';
                                }
                                if (json['more-info'][i]['info_link-size']) {
                                    size = ' (' + json['more-info'][i]['info_link-size'] + ') ';
                                }
                                
                                moreinfo = moreinfo + '<li><a ' + target + ' href=\"' + json['more-info'][i]['info_link-url'] + '\">' + json['more-info'][i]['info_link-title'] + '<span class=\"size\">' + size + '</span></a></li>';
                                
                            }
                            
                            results = results + '<ul class=\"moreInfo\">' + moreinfo + ' </ul>';
                        }
                    }
                    
                });
                results = results + '</div>';
            }
            else {
                results = results + 'This item type has not specified an order :( </div>';
            }
            $('body').append(results);
            
            
        },
        error: function(XMLHttpRequest, textStatus, errorThrown){
            alert("error :" + XMLHttpRequest.responseText);
        }
    });
    
});


function get_type(thing){
    if (thing === null) {
        return "[object Null]";
    }
    return Object.prototype.toString.call(thing).match(/^\[object (.*)\]$/)[1];
}
