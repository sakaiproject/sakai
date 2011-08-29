/*
 stripe the tables, take out when can figure how to do in wicket
 */
var setupTableStriping = function(){
    $('table').each(function(){
        $(this).find('tr:even').addClass('even');
    });
};

/*
 eliminate whitespace only text nodes
 needed to style the wicket pager
 */
jQuery.fn.htmlClean = function(){
    this.contents().filter(function(){
        if (this.nodeType != 3) {
            $(this).htmlClean();
            return false;
        }
        else {
            return !/\S/.test(this.nodeValue);
        }
    }).remove();
};

/*
 add css classes to wicket pager based on block having a link or not
 */
jQuery.fn.cssInstrument = function(){
    $('.pager > span').each(function(i){
        if ($(this).find('a').length === 1) {
            $(this).addClass('linky');
        }
        else {
            $(this).addClass('nolinky');
        }
    });
};

var setupMenus = function(){
    $('tr').mouseenter(function(){
        $(this).find('.actionPanelTrig').show();
    });
    $('tr').mouseleave(function(){
        $(this).find('.actionPanelTrig').hide();
    });
    $('.actionPanelTrig').click(function(e){
        e.preventDefault();
        $(this).closest('tr').addClass('focusedRow');
        var pos = $(this).closest('td.action').position();
        var height = $(this).closest('td').height();
        $('.actionPanel').hide();
        $(this).parent('td').find('.actionPanel').css({
            'position': 'absolute',
            'left': pos.left - 200,
            'top': pos.top + 15
        }).toggle();
    });
    
    $('.actionPanel').mouseleave(function(){
        $(this).hide();
        $(this).closest('tr').removeClass('focusedRow');
    });
};

var setupLinks = function(){
    $('.itemLink').click(function(e){
        e.preventDefault();
        var actionLink = "";
        var action = "";
        var link = $(this).attr('href');
        var title = $(this).text();
        //daft - need better way of identifying type
        var itemType = $(this).closest('tr').find('.itemType').text();
        
        if (itemType === "assignment" || $(this).attr('href').indexOf('assignment') !== -1) {
            //daft 2, neeed a better way of getting the entity id
            action = link.split('/')[9];
            var assigURL = '/direct/assignment/' + action.substring(0, 36) + '.json';
            jQuery.getJSON(assigURL, function(data){
                var results = '<div id=\"metaDataMain\">' + '<strong>' + langdata.due + '</strong> ' + data.dueTimeString + ' (' + langdata.postedBy + data.authorLastModified + ')' + '</div>';
                results = results + '<div id=\"metaDataGradSub\">' + resolveTypeOfGrade(data.content.typeOfGrade) + ', ' + resolveTypeOfSubmission(data.content.typeOfSubmission) + resolveMaxGradePointDisplay(data.content.maxGradePointDisplay) + '</div>';
                results = results + '<div id=\"description\">' + resolveInstructions(data.content.instructions) + '</div>';
                results = results + '<div id=\"link\">' + '<a target ="_top" href=' + link + '>' + langdata.seemore + '</a>' + '</div>';
                $("#dialog").html(results);
            });
            $("#dialog").dialog({
                modal: true,
                width: 400,
                height: 200,
                title: title,
                dialogClass: 'smallDiag',
                close: function(event, ui){
                    $('#dialog >  *').remove();
                }
                
            });
            
        }
        else {
            if (itemType === "announcement" || $(this).attr('href').indexOf('announcement') !== -1) {
                var annURL = link;
                
                /*
                 md table has no border, and is he first one in the responseText
                 attachments is the last paragraph, following a paragraph with only a bold child
                 */
                $.ajax({
                    url: annURL,
                    dataType: 'html',
                    success: function(data){
                        // this is really perverse
                        $('#dialogDum').empty();
                        $('#dialogDum').append(data);
                        var linkList ="";
                        var md = $('#dialogDum').find('table').eq(0);
                        $('#dialogDum').find('table').eq(0).remove();
                        $('#dialogDum').find('h1').remove()
                        $('#dialogDum').find('meta').remove()
                        $('#dialogDum').find('style').remove()
                        $('#dialogDum').find('title').remove()
                        
                        var content = $('#dialogDum').contents();
                        $('#dialog').append((md).find('td').eq(3).text() + ' (' + (md).find('td').eq(1).text() + ')')
                        $('#dialog').append('<hr>')
                        $('#dialog').append(content)
                        if ($('#dialog').find('p').eq(-2).children('b').length === 1) {
                            $('#dialog').append('<ul class=\"attachList\"></ul')
                            $('#dialog').find('p').eq(-1).children('a').each(function(i){
                                var urlArr = $(this).text().split('/')
                                var anchorText = urlArr[urlArr.length-1]
                                linkList = linkList + '<li><a href=\"' + $(this).attr('href') + '\">' + anchorText + '</a></li>';
                            })
                        $('#dialog').find('p').eq(-1).remove();
                        $('#dialog').find('.attachList').append((linkList))

                        }
                        
                    },
                    failure: function(){
                    
                    }
                    
                });
                
                $("#dialog").dialog({
                    modal: true,
                    width: 400,
                    height: 200,
                    title: title,
                    close: function(event, ui){
                        $('#dialog >  *').remove();
                    },
                    dialogClass: 'smallDiag'
                });
            }
            else {
                window.open(link, '_blank');
            }
            
        }
        
    });
};
var resolveTypeOfGrade = function(typeOfGrade){
    switch (typeOfGrade) {
        case 1:
            return langdata.ungraded;
        case 2:
            return langdata.letter;
        case 3:
            return langdata.points;
        case 4:
            return langdata.passfail;
        case 5:
            return langdata.checkmark;
        default:
            return ('wth');
    }
};
var resolveTypeOfSubmission = function(typeOfSubmission){
    switch (typeOfSubmission) {
        case 1:
            return langdata.inline;
        case 2:
            return langdata.atts;
        case 3:
            return langdata.inlineatts;
        case 4:
            return langdata.nonel;
        case 5:
            return langdata.singleatt;
        default:
            return ('wth');
    }
};
var resolveMaxGradePointDisplay = function(maxGradePointDisplay){
    if (maxGradePointDisplay > 0) {
        return (', ' + langdata.maxpoints + maxGradePointDisplay);
    }
    else {
        return "";
    }
};
var resolveInstructions = function(instructions){
    if (instructions !== '') {
        return ('<hr>' + instructions);
    }
    else {
        return (langdata.noinstructions);
    }
    
};
var setupLang = function(){
    langdata = eval('(' + $('#lang-holder').text() + ')');
};

var setupIcons = function(){
    $('.itemLink').each(function(i){
        if ($(this).closest('tr').find('.itemType').text() === 'resource') {
            $(this).addClass(getFileExtension($(this).attr('href')));
        }
    });
    function getFileExtension(filename){
        var ext = /^.+\.([^.]+)$/.exec(filename);
        return ext === null ? "" : ext[1].toLowerCase();
    }
};
