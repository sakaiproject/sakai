$.ajaxSetup({
    cache: false,
    dataType: "json"
});
//not used - the data should come sorted, but it does not
//left here in case all else fails

var sortJSON = function(data, key){
    return data.sort(function(a, b){
        var x = a[key];
        var y = b[key];
        return ((x < y) ? -1 : ((x > y) ? 1 : 0));
    });
};


var iconDecider = function(itemType){
    var path = '/library/image/sakai/';
    switch (itemType) {
        case "application/mac-binhex40":
        case "application/x-tar":
        case "application/zip":
        case "application/x-stuffit":
            return path + 'compressed.gif';

        case "application/msword":
        case "application/rtf":
        case "text/rtf":
            return path + 'word.gif';

        case "application/vnd.ms-excel":
        case "text/csv":
            return path + 'excel.gif' ;

        case "image/vnd.adobe.photoshop":
        case "application/x-photoshop":
            return path + 'photoshop.gif';

        case "image/gif":
        case "image/png":
        case "image/jpeg":
            return path + 'image.gif';

        case "video/mpeg":
        case "video/quicktime":
        case "video/msvideo":
        case "video/avi":
        case "video/x-msvideo":
            return path + 'movie.gif';

        case "audio/mpeg":
        case "audio/x-midi":
            return path + 'audio.gif';

        case "application/pdf":
            return path + 'pdf.gif';
        case "application/postscript":
            return path + 'postscript.gif';
        case "application/vnd.ms-powerpoint":
            return path + 'ppt.gif';
        case "application/x-msaccess":
            return path + 'access.gif';
        case "application/x-shockwave-flash":
            return path + 'shockwave.gif';
        case "audio/x-pn-realaudio":
            return path + 'real.gif';
        case "text/html":
            return path + 'html.gif';
        case "text/plain":
            return path + 'text.gif';
        case "text/x-java-source":
            return path + 'java.gif';
        default:
            return path + 'generic.gif';
    }
};

var renderHierarchyWithJsonTree = function(data){

    var collId = $('#collectionId').val();
    var siteId = collId.split('/')[2];
    //massage the json so that we can use jsonTree
    $.each(data.content_collection, function(i, item){
        item.text = item.title;
        //get item.id field to turn into a jsTree happy item.id
        item.id = item.url.substr(item.url.indexOf('/content/group/' + siteId));
        
        //transforming item.container into item.parent
        if (item.type === 'collection'){
            item.id = item.id.substring(0,item.id.length -1);
        } 
        var itemIdArr = item.id.split('/');
        var parentPath = item.id.split('/').slice(0,itemIdArr.length - 1).join('_');
        item.id = item.id.replace(/\//g, "_");
        //if this is site root, special parent value
        // since the site root title is null in /direct
        // give it the site title
        if (item.container === '/content/group/') {
            item.parent = '#';
            //TODO: some sites report 'null' for title, use the Id - need better handle
            if (item.title === null){
                item.text = siteId;
            }
            else {
                item.text = item.title;
            }
            
            
        }
        else {
            item.parent = parentPath;
        }
        var pathArr = item.url.split('/');
        var siteIdLoc = pathArr.indexOf(siteId);
        var pathToFolder = decodeURIComponent('/group/' + pathArr.slice(siteIdLoc).join('/'));
        var pathToFile = item.url;
        var itemType = '';
        var itemUrl = '';
        
        //depending on file or collection type, custom parameters
        if (item.type === 'collection') {
            var itemText ='';
            if(item.numChildren ===1){
                itemText = ' ' + jsLang.item;
            }
            if(item.numChildren ===0 || item.numChildren > 1){
                itemText = ' ' + jsLang.items;
            }
            itemType = 'folder';
            item.text = item.text + '&nbsp;&nbsp;&nbsp;<small class="muted">(' + item.numChildren + itemText + ')</small>';
            itemUrl = pathToFolder;
        }
        else {
            itemType = 'file';
            item.icon = iconDecider(item.type);
            itemUrl = pathToFile;
        }
        //adding custom parameters for URL and TYPE
        item.li_attr = {
            'data_url': itemUrl,
            'data_type': itemType,
            'data_children': item.numChildren,
            'data_visible': item.visible
        };
    });
    
    $(function(){
        $("#navigatePanelInner").on('changed.jstree', function(e, data){
            var selectedId = data.selected[0];
            //if the clicked link is a file - launch the /access link to it
            //TODO: will this trigger the copyright aggreement form?
            if (data.node.li_attr.data_type === 'file') {
                var launchURL = data.node.li_attr.data_url;
                window.open(launchURL);
            }
            else {
                var folderUrl = data.node.li_attr.data_url;
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
                'multiple': false,
                'data': data.content_collection,
                'themes': {
                    'stripes': true,
                    'theme': 'default',
                    'dots': true,
                    'icons': true
                }
            },
            "search": {
                "case_insensitive": true
            },
            "plugins": ["themes", "search"]
        });
    });
    $('#navigatePanelInner').jstree('open_node', '_content_group_' + siteId);
    $('#navigatePanel').fadeIn('slow');
};
/*
show spinner whenever async actvity takes place
 */
$(document).ajaxStart(function(){
    $('#spinner').show();
});
$(document).ajaxStop(function(){
    $('#spinner').hide();
});

$(document).ready(function(){
    var to = false;
    $('#navigatePanelSearch').keyup(function(){
        if (to) {
            clearTimeout(to);
        }
        to = setTimeout(function(){
            var v = $('#navigatePanelSearch').val();
            $("#navigatePanelInner").jstree("search", v);
        }, 250);
    });

    if ($('#content_print_result_url').length) {
        window.open($('#content_print_result_url').val(), $('#content_print_result_url_title'), "height=800,width=800");
    }
    $('.portletBody').click(function(e){
        if (e.target.className != 'menuOpen' && e.target.className != 'dropdn') {
            $('.makeMenuChild').hide();
        }
        else {
            if (e.target.className == 'dropdn') {
                $('.makeMenuChild').hide();
                $(e.target).parent('li').find('ul').show().find('li:first a').focus();
                
            }
            else {
                $('.makeMenuChild').hide();
                $(e.target).find('ul').show().find('li:first a').focus();
            }
        }
    });
    
    $('#navigatePanel p.close').on('click', function(){
        $('.keep-open').removeClass('open');
    });
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
    
    $('#navigatePanelInnerCollapse').on('click', function(e){
        e.preventDefault();
        $('#navigatePanelInner').jstree('close_all');
    });
    $('#navigatePanelInnerExpand').on('click', function(e){
        e.preventDefault();
        $('#navigatePanelInner').jstree('open_all');
    });
    
    var collId = $('#collectionId').val();
    collId = collId.substring(0, collId.length - 1);
    // construct a url to /direct based on current site
    var url = '/direct/content' + collId.replace('/group/', '/site/') + '.json';
    $('#navigate').click(function(){
        if ($('#navigatePanelInner ul').length === 0) {
            var jqxhr = $.getJSON(url, function(data){
                if (data.content_collection.length) {
                    renderHierarchyWithJsonTree(data);
                }
                else {
                    $('#navigatePanelInner').html('<div class="alert alert-danger"> ERROR! No entity feeds for collection. </div>');
                }
            }).done(function(){
            }).fail(function(){
            }).always(function(){
            });
        }
    });
    
    $('.dropdown.keep-open').on({
        "shown.bs.dropdown": function(){
            $(this).data('closable', true);
        },
        "click": function(){
            $(this).data('closable', false);

        },
        "hide.bs.dropdown": function(){
            return $(this).data('closable');
        }
    });
    
});
