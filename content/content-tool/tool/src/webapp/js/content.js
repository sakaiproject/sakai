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
            return path + 'compressed.gif';
        case "application/msword":
            return path + 'word.gif';
        case "application/pdf":
            return path + 'pdf.gif';
        case "application/postscript":
            return path + 'postscript.gif';
        case "application/rtf":
            return path + 'word.gif';
        case "application/vnd.ms-excel":
            return path + 'excel.gif';
        case "application/vnd.ms-powerpoint":
            return path + 'ppt.gif';
        case "application/x-msaccess":
            return path + 'access.gif';
        case "application/x-shockwave-flash":
            return path + 'shockwave.gif';
        case "application/x-stuffit":
            return path + 'compressed.gif';
        case "application/x-tar":
            return path + 'compressed.gif';
        case "application/zip":
            return path + 'compressed.gif';
        case "audio/mpeg":
            return path + 'audio.gif';
        case "audio/x-pn-realaudio":
            return path + 'real.gif';
        case "image/gif":
            return path + 'image.gif';
        case "image/png":
            return path + 'image.gif';
        case "image/vnd.adobe.photoshop":
            return path + 'photoshop.gif';
        case "text/csv":
            return path + 'excel.gif';
        case "text/html":
            return path + 'html.gif';
        case "text/plain":
            return path + 'text.gif';
        case "text/x-java-source":
            return path + 'java.gif';
        case "video/mpeg":
            return path + 'movie.gif';
        case "video/quicktime":
            return path + 'movie.gif';
        default:
            return path + 'default.gif';
    }
};

var renderHierarchyWithJsonTree = function(data){

    var collId = $('#collectionId').val();
    var siteId = collId.split('/')[2];
    
    //massage the json so that w can use jsonTree
    $.each(data.content_collection, function(i, item){
        item.text = item.title;
        //get item.id field to turn into a jsTree happy item.id
        var itemUrl = item.url;
        item.id = itemUrl.substr(itemUrl.indexOf('/content/group/' + siteId)).replace(/\//g, "_");
        //transforming item.container into item.parent 
        var parentPath = item.container;
        //if this is site root, special parent value
        // since the site root title is null in /direct
        // give it the site title
        if (parentPath === '/content/group/') {
            item.parent = '#';
            item.text = siteId
        }
        else {
            item.parent = parentPath.replace(/\//g, "_");
        }  
        
        path = itemUrl.substr(itemUrl.indexOf('/content/group/' + siteId) + 23);
        var itemType='';
        var itemUrl='';
        //depending on file or collection type, custom parameters
        if (item.type ==='collection'){
            itemType= 'folder';
            itemUrl = '/group/' + siteId + '/' + path.substr(0, path.lastIndexOf('/')) + '/';
        }
        else {
            itemType= 'file';
            item.icon = iconDecider(item.type);
            itemUrl = item.url
        }
        //adding custom parameters for URL and TYPE
        item.li_attr = {'data_url':itemUrl,'data_type': itemType};
    });
    
    $(function(){
        $("#navigatePanelInner").on('changed.jstree', function(e, data){
            var selectedId = data.selected[0];
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
                'data': data.content_collection,
                'themes': {
                    'theme': 'default',
                    'dots': false,
                    'icons': true
                },
                "search": {
                    "case_insensitive": true
                },
                "plugins": ["themes", "html_data", "search", "adv_search"]
            }
        });
    });
    $('#navigatePanelInner').jstree('open_node', '_content_group_' + siteId + '_');
    $('#navigatePanel').fadeIn('slow');
};

$(document).ready(function(){
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
    
    $('#navigatePanelInnerCollapse').on('click', function(){
        $('#navigatePanelInner').jstree('close_all');
    });
    $('#navigatePanelInnerExpand').on('click', function(){
        $('#navigatePanelInner').jstree('open_all');
    });
    
    
    // this should come from a context variable you bonehead
    //ye gods
    var collId = $('#collectionId').val();
    collId = collId.substring(0, collId.length - 1);
    var url = '/direct/content/' + collId.replace('/group/', '/site/') + '.json';
    $('#navigate').click(function(){
        if ($('#navigatePanelInner ul').length === 0) {
            var jqxhr = $.getJSON(url, function(data){
                renderHierarchyWithJsonTree(data);
            }).done(function(){
            }).fail(function(){
            }).always(function(){
            });
        }
    });
    
    $('.dropdown-backdrop').on('click', function(e){
        $('.dropdown-toggle').dropdown();
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
