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
    var path = 'fa fa-';
    switch (itemType) {
        case "application/mac-binhex40":
        case "application/x-tar":
        case "application/zip":
        case "application/x-stuffit":
            return path + 'file-archive-o';
            
        case "application/msword":
        case "application/rtf":
        case "text/rtf":
            return path + 'file-word-o';
            
        case "application/vnd.ms-excel":
        case "text/csv":
            return path + 'file-excel-o';
            
        case "image/vnd.adobe.photoshop":
        case "application/x-photoshop":
            return path + 'file-o';
            
        case "image/gif":
        case "image/png":
        case "image/jpeg":
            return path + 'file-image-o';
            
        case "video/mpeg":
        case "video/quicktime":
        case "video/msvideo":
        case "video/avi":
        case "video/x-msvideo":
            return path + 'file-video-o';
            
        case "audio/mpeg":
        case "audio/x-midi":
            return path + 'file-audio-o';
            
        case "application/pdf":
            return path + 'file-pdf-o';
        case "application/postscript":
            return path + 'file-o';
        case "application/vnd.ms-powerpoint":
            return path + 'file-powerpoint-o';
        case "application/x-msaccess":
            return path + 'file-o';
        case "application/x-shockwave-flash":
            return path + 'file-o';
        case "audio/x-pn-realaudio":
            return path + 'file-o';
        case "text/html":
            return path + 'file-code-o';
        case "text/plain":
            return path + 'file-text-o';
        case "text/x-java-source":
            return path + 'file-code-o';
        default:
            return path + 'file-o';
    }
};

var renderHierarchyWithJsonTree = function(data){
    //root folder
    var collId = $('#collectionId').val();
    // site id
    var siteId = collId.split('/')[2];
    //workspace or  site
    var mode = collId.split('/')[1];
    //massage the json so that we can use jsonTree
    var user;
    $.each(data.content_collection, function(i, item){
        item.text = escapeHtml(item.title);
        //get item.id field to turn into a jsTree happy item.id
        if (mode === 'group') {
            item.id = item.url.substr(item.url.indexOf('/group/' + siteId));
        }
        if (mode === 'user') {
            item.id = item.url.substr(item.url.indexOf('/user/'));
        }
        
        //transforming item.container into item.parent
        if (item.type === 'collection') {
            item.id = item.id.substring(0, item.id.length - 1);
        }
        var itemIdArr = item.id.split('/');
        var parentPath = item.id.split('/').slice(0, itemIdArr.length - 1).join('_');
        item.id = item.id.replace(/\//g, "_");

        //if this is site root, special parent value
        // since the site root title is null in /direct
        // give it the site title
        
        if (item.container === '/content/group/' || item.container === '/content/user/') {
            item.parent = '#';
            //TODO: some sites report 'null' for title, use the Id - need better handle
            if (item.title === null) {
                item.text = siteId;
            }
            else {
                item.text = item.title;
            }
        }
        else {
            item.parent = parentPath;
        }
        var pathToFolder ='';
        var pathArr = item.url.split('/');
        var siteIdLoc='';
        if (mode === 'group') {
            siteIdLoc = pathArr.indexOf(siteId);
            pathToFolder  = '/group/' + decodeURIComponent(pathArr.slice(siteIdLoc).join('/'));
        }
        if (mode === 'user') {
              siteIdLoc = pathArr.indexOf('user') + 2;
              user = pathArr[pathArr.indexOf('user') + 1];
              pathToFolder = '/user/'+ siteId + '/' + decodeURIComponent(pathArr.slice(siteIdLoc).join('/'));
        }
        var pathToFile = item.url;
        var itemType = '';
        var itemUrl = '';
        
        //depending on file or collection type, custom parameters
        if (item.type === 'collection') {
            var itemText = '';
            if (item.numChildren === 1) {
                itemText = ' ' + jsLang.item;
            }
            if (item.numChildren === 0 || item.numChildren > 1) {
                itemText = ' ' + jsLang.items;
            }
            itemType = 'folder';
            item.text = item.text;
            itemUrl = pathToFolder;
            item.icon = (item.numChildren > 0) ? 'fa fa-folder' : 'fa fa-folder-o';
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
                    'theme': 'default',
                    'stripes': true,
                    'dots': true,
                    'icons': true
                }
            },
            "search": {
                "case_insensitive": true,
                "fuzzy": false,
                "show_only_matches": true
            },
            "plugins": ["themes", "search"]
        });
    });
    // what folders to show when the panel first open
    // here we are opening hte first levl if any so that it
    //is the same as the resources tool proper
    if (mode === 'group') {
        $('#navigatePanelInner').jstree('open_node', '_group_' + siteId);
    }
    else {
        $('#navigatePanelInner').jstree('open_node', '_user_' + user);
    }
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

var escapeHtml = function (str) {
    return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;') ;
}

$(document).ready(function(){

    $('[data-toggle="popover"]').popover()


    $('#navigate').prop('disabled',false)
    $('#navigatePanelSearch').keyup(function(){
         var v = $('#navigatePanelSearch').val();
         if (v.length >= 2) {
             $("#navigatePanelInner").jstree("search", v);
         }
         else {
             $("#navigatePanelInner").jstree("search", '');
         }
    });
    
    if ($('#content_print_result_url').length) {
        window.open($('#content_print_result_url').val(), $('#content_print_result_url_title'), "height=800,width=800");
    }
    
    $('#navigatePanel p.close').on('click', function(){
        $('.keep-open').removeClass('open');
    });

    
    $('#navigatePanelInnerCollapse').on('click', function(e){
        e.preventDefault();
        $('#navigatePanelInner').jstree('close_all');
        $(this).parent('.expand_collapse').find('button').toggle();
    });
    $('#navigatePanelInnerExpand').on('click', function(e){
        e.preventDefault();
        $('#navigatePanelInner').jstree('open_all');
        $(this).parent('.expand_collapse').find('button').toggle();
    });
    
    var collId = $('#collectionId').val();
    collId = collId.substring(0, collId.length - 1).split('/');
    var rootId =  '/' + collId[1] + '/' + collId[2];
    // construct a url to /direct based on current site
    var url = '/direct/content' + rootId.replace('/group/', '/site/') + '.json';
    $('#navigate').click(function(){
        if ($('#navigatePanelInner ul').length === 0) {
            var jqxhr = $.getJSON(url, function(data){
                if (data.content_collection.length) {
                    renderHierarchyWithJsonTree(data);
                }
                else {
                    $('#navigatePanelInner').html('<div class="alert alert-danger">' + jsLang.error + '</div>');
                }
            }).done(function(){
            }).fail(function(){
                $('#navigatePanelInner').html('<div class="alert alert-danger">' + jsLang.error + '</div>');
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
    if ($('.resourcesList').length === 1) {
      setupColumnToggle();
    }
});

var setupColumnToggle = function(){

    var massageColWidths = function(){
        if ($('#columnTog :checked').length === 0) {
            $('.actions2').css({
                'width': '30%'
            });
        }
        else {
            $('.actions2').css('width', '18px');
        }
    };
    
    var setupColUI = function(data){
        // hides columns and sets the checkbox values
        $.each(data, function(key, value){
            if (value === 'false' || value === false) {
                $('.' + key).hide();
                $('#' + key + 'Tog').find('input').attr('checked', false);
            }
            else {
                $('#' + key + 'Tog').find('input').attr('checked', true);
            }
            massageColWidths();
        });
    };
    
    var jsonify = function(str){
        var jsonObj = {};
        var keyVals = str.split('&');
        var keyValsLength = keyVals.length;
        for (var i = 0; i < keyValsLength; i++) {
            var thiskeyVal = keyVals[i].split('=');
            if (thiskeyVal[1] === 'true') {
                thiskeyVal[1] = true;
            }
            else {
                thiskeyVal[1] = false;
            }
            jsonObj[thiskeyVal[0]] = thiskeyVal[1];
        }
        //return jsonObj
        return JSON.stringify(jsonObj);
    };

    var writeDOMVal = function(name, val){
        if (window.localStorage) {
            sessionStorage.setItem([name], val);
        }
    };
    
    var readDOMVal = function(name){
        if (window.localStorage) {
            return sessionStorage.getItem([name]);
        }
    };
    
    var readDBVal = function(name){
        // name = resourcesColumn
        $.ajax({
            type: 'GET',
            url: '/direct/userPrefs/key/' + $('#userId').text() + '/' + name + '.json',
            cache: false,
            dataType: 'json'
        }).done(function(data){
            // this callback will use the data sent to write to the DOM;
            $(data).each(function(key,val){
                $.each(val,function(k,v){
                    if (k === "data")
                    {
                        writeDOMVal('resourcesColumn', JSON.stringify(v));
                        setupColUI(v);
                    }
                });
            });
        }).fail(function(){
            // checkboxes will be all checked and columns will all show
        });
    };
    
    var writeDBVal = function(name, val){
        // use userPrefs call to preserve choices into db
        // name will be the setting type (i.e.'resourcesColumn')
        // val will be a prepared query string like 'access=true&creator=true&modified=true&size=true'
        jQuery.ajax({
            type: 'PUT',
            url: "/direct/userPrefs/updateKey/" + $('#userId').text() + "/" + name + "?" + val
        }).done(function(data){
        }).fail(function(){
            // TODO: maybe message to user that
        });
    };
    
    var val= "";
    //setting up on page load
    if (readDOMVal('resourcesColumn') === null) {
        // DOM storage null - ask the db and write to the DOM in the callback')
        // if the DB is null write to it with the known set with all key set to false
        // check all the checkboxes
        readDBVal('resourcesColumn');
    }
    else {
        // use DOM values
        val = $.parseJSON(readDOMVal('resourcesColumn'));
        setupColUI(val);
    }
    $('#columnTog label').click(function(e){
        e.stopPropagation();
    });
    $('#columnTog input').click(function(e){
        var target = $(this).closest('span').attr('id').replace('Tog', '');
        e.stopPropagation();
        if ($(this).prop('checked') === true) {
            $('.' + target).show();
        }
        else {
            $('.' + target).hide();
        }
        massageColWidths();
    });
    
    $('#columnTog #saveCols').click(function(e){
        e.preventDefault();
        var str = '';
        $("#columnTog input").each(function(index){
            str = str + $(this).closest('span').attr('id').replace('Tog', '') + '=' + $(this).prop('checked');
            //  obj[$(this).closest('span').attr('id').replace('Tog', '')] = $(this).prop('checked');
            if (index !== 3) {
                str = str + '&';
            }
        });
        
        // update DOM
        writeDOMVal('resourcesColumn', jsonify(str));
        
        //store str in db
        writeDBVal('resourcesColumn', str);
    });
};
