$(document).ready(function() {
// add a special date parser through the tablesorter addParser method
    $.tablesorter.addParser({         
        // set a unique id
        id: 'realDate',
        is: function(s) {
             return false; //so this parser is not auto detected
        },
        format: function(s) {
            // format the date data for normalization
            return $.tablesorter.formatFloat(new Date(s.replace(/-/g,' ').replace(':00.0','')).getTime());
        },
        // set type, either numeric or text
        type: 'numeric'
    });

    //A filter to extract the real date and pass that to the sorter
    var dateExtractor = function(_that) {
       var that = $(_that);
        if(that.attr('name') && (that.attr('name').search(/realDate:/) != -1)){
            return that.attr('name').replace(/realDate:/,'');
        }
       return that.text();
    }
    $("#sortableTable").tablesorter({
        sortList: [[2,1]], // Initial order by the latest (NOT earliest) closing date
        headers: { // disable it by setting the property sorter to false
            1: {sorter: 'realDate'},
            2: {sorter: 'realDate'},
            3: {sorter: false},
            4: {sorter: false}
        },
        textExtraction: dateExtractor
    });
});