$(document).ready(function() {
    //A filter to extract the real date formatted as a simple number and pass that to the sorter
    var dateExtractor = function(_that) {
       var that = $(_that);
        if(that.attr('name') && (that.attr('name').search(/realDate:/) != -1)){
            return that.attr('name').replace(/realDate:/,'').replace(/-/g,' ').replace(':00.0','').replace(/:/g,'').replace(/ /g,'');
        }
       return that.text();
    }
    $("#sortableTable").tablesorter({
        sortList: [[2,1]], // Initial order by the latest (NOT earliest) closing date
        headers: { // disable it by setting the property sorter to false
            1: {sorter: 'digit'},
            2: {sorter: 'digit'},
            3: {sorter: false},
            4: {sorter: false}
        },
        textExtraction: dateExtractor
    });
});