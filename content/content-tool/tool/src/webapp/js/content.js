//not used - the data should come sorted, but it does not
//left here in case all else fails
var sortJSON = function(data, key){
    return data.sort(function(a, b){
        var x = a[key];
        var y = b[key];
        return ((x < y) ? -1 : ((x > y) ? 1 : 0));
    });
}

var iconDecider = function(itemType){
    var path = '/library/image/sakai/'
    switch (itemType) {
        case "application/mac-binhex40":
            return path  + 'compressed.gif';
            break;
        case "application/msword":
            return path  + 'word.gif';
            break;
        case "application/pdf":
            return path  + 'pdf.gif';
            break;
        case "application/postscript":
            return path + 'postscript.gif';
            break;
        case "application/rtf":
            return path  + 'word.gif';
            break;
        case "application/vnd.ms-excel":
            return path + 'excel.gif';
            break;
        case "application/vnd.ms-powerpoint":
            return path + 'ppt.gif';
            break;
        case "application/x-msaccess":
            return path + 'access.gif';
            break;
        case "application/x-shockwave-flash":
            return path + 'shockwave.gif';
            break;
        case "application/x-stuffit":
            return path + 'compressed.gif';
            break;
        case "application/x-tar":
            return path + 'compressed.gif';
            break;
        case "application/zip":
            return path + 'compressed.gif';
            break;
        case "audio/mpeg":
            return path + 'audio.gif';
            break;
        case "audio/x-pn-realaudio":
            return path + 'real.gif';
            break;
        case "image/gif":
            return path + 'image.gif';
            break;
        case "image/png":
            return path + 'image.gif';
            break;
        case "image/vnd.adobe.photoshop":
            return path + 'photoshop.gif';
            break;
        case "text/csv":
            return path + 'excel.gif';
            break;
        case "text/html":
            return path + 'html.gif';
            break;
        case "text/plain":
            return path + 'text.gif';
            break;
        case "text/x-java-source":
            return path + 'java.gif';
            break;
        case "video/mpeg":
            return path + 'movie.gif';
            break;
        case "video/quicktime":
            return path + 'movie.gif';
            break;
        default:
            return path + 'default.gif';
    }
}


var renderHierarchyWithJsonTree = function(data){
    
    var collId = $('#collectionId').val()
    siteId = collId.split('/')[2]
    
    
    //massage the json so that w can use jsonTree
    var folderList = []
    $.each(data.content_collection, function(i, item){
        //add item.text field
        item.text = item.title;
        
        //get item.id field
        var itemUrl = item.url;
        var path = itemUrl;
        //appending a bogus string so that we can tell when a click event
        //is on a document or on a folder
        item.id = 'fileitem' + path.substr(itemUrl.indexOf('/content/group/' + siteId)).replace(/\//g, "_");
        
        //transforming item.container into item.parent 
        var parentPath = item.container;
        item.parent = parentPath.replace(/\//g, "_");
      
        item.icon = iconDecider (item.type)
        
        //data has no notion of folders
        //so here we are grabbing a path, massaging the text to construct a folder item
        var path = itemUrl.substr(itemUrl.indexOf('/content/group/' + siteId) + 23);
        //haha more hardwiring to the mercury site!
        var folderId = '/content/group/' + siteId +'/' + path.substr(0, path.lastIndexOf('/')) + '_';
        
        //this does not seem to work
        item.a_attr = path
        //add this folder to the folder array - there will be duplicates - this is dumb
        folderList.push(folderId);
    });
    
    //remove all the dupes in the folder array
    var uniqueIds = [];
    $.each(folderList, function(i, el){
        if ($.inArray(el, uniqueIds) === -1) 
            uniqueIds.push(el);
    });
    folderList = uniqueIds.sort();
    
    
    //for each folder id item, construct an oject that jsTree will unerstand
    // lots of idiotic string parsing to get the item.id, item.text and item.parent values
    $.each(folderList, function(i, el){
        var pathArray = el.split('/');
        var folder = pathArray.pop();
        folder = folder.substr(0, folder.length - 1);
        var parentFolder = pathArray.join('/');
        //good lord - this is hard wired to the mercury site -double plus bad
        if (parentFolder === undefined || parentFolder === '' || parentFolder === '/content/group/' + siteId) {
            parentFolder = "#"
        }
        else {
            parentFolder = parentFolder.replace(/\//g, "_") + '_';
        }
        newItem = {};
        newItem.id = el.replace(/\//g, "_");
        newItem.parent = parentFolder;
        newItem.text = folder;
        newItem.a_attr = el;
        //adding this object to the collection
        data.content_collection.push(newItem);
    });
    
    //invoke jsTree with the massaged data
    $('#navigatePanel').on('changed.jstree', function(e, data){
        //event listener
        // this will return the path after a lot of imbecilic string massaging
        var selectedId = data.selected[0];
        
        //here we are using the bogus appended string to decide 
        // if this is a folder or a darn document
        if (selectedId.indexOf('fileitem') === 0) {
            //more exciting string wrangling
            var launchURL = selectedId.replace(/_/g, "\/").replace('fileitem', '/access/')
            window.open(launchURL)
        }
        else {
            //what is the folderUrl? only more string strangling will tell
            var folderUrl = selectedId.replace(/_/g, "\/").replace('/content', '')
            // here we are populating the form associated with resources list
            $('#sakai_action').val('doNavigate');
            $('#collectionId').val(folderUrl);
            $('#navRoot').val('');
            // and after populating the values we submit the form to navigate
            // to the folder
            $('#showForm').submit();
        }
    }).jstree({
        'core': {
            'data': data.content_collection,
            'themes': {
                'theme': 'default',
                'dots': false,
                'icons': true
            },
            'plugins': ['sort']
        }
    });
    
    $('#navigatePanel').fadeIn('slow')
};

var setupColumnToggle = function(){
    var cols = ['accessTog', 'creatorTog', 'modifiedTog', 'sizeTog', 'allTog']
    var colspan = 0;
    $.each(cols, function(i, val){
        var target = val.replace('Tog', '');
        if (readDOMVal(target) === 'true') {
            $('.' + target).hide();
            $('#' + val).attr('data-status', 'show');
            $('#' + val).attr('class', 'colShow');
        }
        else {
            $('#' + val).attr('data-status', 'hide');
            $('#' + val).attr('class', 'colHide');
            ++colspan
        }
    });
    if (colspan <= 1) {
        $('.colspan').hide();
    }
    else {
        $('.colspan').show();
    }
    
    $('#columnTog a').click(function(e){
        e.preventDefault(e);
        if ($(this).attr('id') === 'allTog') {
            if ($(this).attr('data-status') === 'show') {
                $(this).attr('data-status', 'hide');
                $(this).attr('class', 'colHide');
                $.each(cols, function(i, val){
                    var target = val.replace('Tog', '');
                    $('.' + target).show();
                    $('#' + val).attr('data-status', 'hide');
                    $('#' + val).attr('class', 'colHide');
                    writeDOMVal(target, 'false');
                });
                $('.colspan').show();
            }
            else {
                $(this).attr('data-status', 'show');
                $(this).attr('class', 'colShow');
                $.each(cols, function(i, val){
                    var target = val.replace('Tog', '');
                    $('.' + target).hide();
                    $('#' + val).attr('data-status', 'show');
                    $('#' + val).attr('class', 'colShow');
                    writeDOMVal(target, 'true');
                    $('.colspan').hide();
                });
            }
        }
        else {
        
            var target = $(this).attr('id').replace('Tog', '');
            if ($(this).attr('data-status') === 'show') {
                $(this).attr('data-status', 'hide');
                $(this).attr('class', 'colHide');
                $('.' + target).show();
                writeDOMVal(target, 'false');
                ++colspan
            }
            else {
                $(this).attr('data-status', 'show');
                $(this).attr('class', 'colShow');
                $('.' + target).hide();
                writeDOMVal(target, 'true');
                --colspan
            }
            if (colspan === 1) {
                $('.colspan').hide();
            }
            else {
                $('.colspan').show();
            }
        }
    });
}

var readDOMVal = function(name){
    if (window.localStorage) {
        return sessionStorage.getItem([name]);
    }
};
var writeDOMVal = function(name, val){
    if (window.localStorage) {
        sessionStorage.setItem([name], val);
    }
};
$(document).ready(function(){
    if ($('#content_print_result_url').length) {
        window.open($('#content_print_result_url').val(), $('#content_print_result_url_title'), "height=800,width=800");
    }
    $('.toggleDescription').click(function(e){
        e.preventDefault();
        $('.descPanel').css({
            'top': '-1000px',
            'left': '-1000px',
            'display': 'none'
        }).attr({
            'aria-hidden': 'true',
            'tabindex': '-1'
        });
        $(this).next('div').css({
            'top': e.pageY + 10,
            'left': e.pageX + 10,
            'cursor': 'pointer',
            'display': 'block'
        }).attr({
            'aria-hidden': 'false',
            'tabindex': '0'
        });
    });
    $('.descPanel').blur(function(){
        $(this).css({
            'top': '-1000px',
            'left': '-1000px',
            'display': 'none'
        }).attr({
            'aria-hidden': 'true',
            'tabindex': '-1'
        });
    });
    $('.descPanel').click(function(){
        $(this).css({
            'top': '-1000px',
            'left': '-1000px',
            'display': 'none'
        }).attr({
            'aria-hidden': 'true',
            'tabindex': '-1'
        });
    });
    setupColumnToggle();
    
    // this should come from a context variable you bonehead
    
    //ye gods
    var collId = $('#collectionId').val()
    collId = collId.substring(0, collId.length - 1);
    url = '/direct/content/' + collId.replace('/group/','/site/') + '.json'
    $('#navigate').click(function(){
        if ($('#navigatePanel ul').length === 0) {
            var jqxhr = $.getJSON(url, function(data){
                renderHierarchyWithJsonTree(data);
            }).done(function(){
            }).fail(function(){
            }).always(function(){
            });
        }
    })
})

