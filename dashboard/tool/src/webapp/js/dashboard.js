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

//toggle a fade
jQuery.fn.fadeToggle = function(speed, easing, callback){
    return this.animate({
        opacity: 'toggle'
    }, speed, easing, callback);
    
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
    //ignore 
    $('.itemLink').click(function(e){
        e.preventDefault();
        var actionLink = "";
        var action = "";
        var link = $(this).attr('href');
        var title = $(this).text();
        var parentRow = $(this).closest('tr');
        var colCount = $(parentRow).find('td').length
        var parentCell = $(this).closest('td')
        //daft - need better way of identifying type
        var itemType = $(this).closest('tr').find('.itemType').text();
        if ($(parentRow).next('tr.newRow').length === 1) {
            $(parentRow).next('tr.newRow').find('.results').fadeToggle('slow', '', function(){
                if ($(parentRow).next('tr.newRow').find('.results:visible').length === 0) {
                    $(parentCell).attr('class', '')
                }
                else {
                    $(parentCell).addClass('activeCell');
                }
            })
        }
        else {
            if (itemType === "assignment" || $(this).attr('href').indexOf('assignment') !== -1) {
                //daft 2, neeed a better way of getting the entity id
                action = link.split('/')[9];
                var assigURL = '/direct/assignment/' + action.substring(0, 36) + '.json';
                jQuery.ajax({
                    url: assigURL,
                    dataType: 'json',
                    success: function(data){
                        var results = '<div class=\"results\" style=\"display:none\"><div id=\"metaDataMain\">' + '<strong>' + langdata.due + '</strong> ' + data.dueTimeString + ' (' + langdata.postedBy + data.authorLastModified + ')' + '</div>';
                        results = results + '<div id=\"metaDataGradSub\">' + resolveTypeOfGrade(data.content.typeOfGrade) + ', ' + resolveTypeOfSubmission(data.content.typeOfSubmission) + resolveMaxGradePointDisplay(data.content.maxGradePointDisplay) + '<div id=\"link\">' + '<a target ="_top" href=' + link + '>' + langdata.seemore + '</a>' + '</div>' + '</div>';
                        results = results + '<div id=\"description\">' + resolveInstructions(data.content.instructions) + '</div></div>';
                        
                        $('.activeCell').removeClass('.activeCell')
                        $(parentCell).addClass('activeCell');
                        $('<tr class=\"newRow\"><td colspan=\"' + colCount + '\">' + results + '</td></tr>').insertAfter(parentRow)
                        $('.newRow').find('.results').slideDown('slow', function(){
                            resizeFrame('grow')
                        });
                        
                    },
                    error: function(){
                        $('<tr class=\"newRow\"><td colspan=\"' + colCount + '\"><div class=\"results\">Error retriving data - assignment may have had attachments</div></td></tr>').insertAfter(parentRow)
                        $('.newRow').find('.results').slideDown('slow', function(){
                            resizeFrame('grow')
                        });
                        
                    }
                });
                
                
                /*
                 $("#dialog").dialog({
                 modal: false,
                 width: 400,
                 height: 200,
                 title: title,
                 dialogClass: 'smallDiag',
                 close: function(event, ui){
                 $('#dialog >  *').remove();
                 }
                 
                 });
                 */
            }
            else {
                if (itemType === "announcement" || $(this).attr('href').indexOf('announcement') !== -1) {
                    var annURL = link;
                    $.ajax({
                        url: annURL,
                        dataType: 'html',
                        success: function(data){
                            // this is really perverse
                            $('#dialogDum').empty();
                            $('#dialog').empty();
                            $('#dialogDum').append(data);
                            var linkList = "";
                            var md = $('#dialogDum').find('table').eq(0);
                            $('#dialogDum').find('table').eq(0).remove();
                            $('#dialogDum').find('h1').remove()
                            $('#dialogDum').find('meta').remove()
                            $('#dialogDum').find('style').remove()
                            $('#dialogDum').find('title').remove()
                            
                            var content = $('#dialogDum').contents();
                            
                            $('#results').append((md).find('td').eq(3).text() + ' (' + (md).find('td').eq(1).text() + ')')
                            $('#results').append('<hr>')
                            $('#results').append(content)
                            if ($('#results').find('p').eq(-2).children('b').length === 1) {
                                $('#results').append('<ul class=\"attachList\"></ul')
                                $('#results').find('p').eq(-1).children('a').each(function(i){
                                    var urlArr = $(this).text().split('/')
                                    var anchorText = urlArr[urlArr.length - 1]
                                    linkList = linkList + '<li><a href=\"' + $(this).attr('href') + '\">' + anchorText + '</a></li>';
                                })
                                $('#results').find('p').eq(-1).remove();
                                $('#results').find('.attachList').append((linkList))
                            }
                            var results = $('#results').html();
                            $('.activeCell').removeClass('.activeCell')
                            $(parentCell).addClass('activeCell');
                            $('<tr class=\"newRow\"><td colspan=\"' + colCount + '\"><div class=\"results\" style=\"display:none\">' + results + '</div></td></tr>').insertAfter(parentRow)
                            $(parentRow).next('tr').find('.results').slideDown('slow', function(){
                                resizeFrame('grow')
                            });
                            
                            
                        },
                        error: function(){
                        
                            $('<tr class=\"newRow\"><td colspan=\"' + colCount + '\"><div class=\"results\" style=\"display:none\">There was an error retrieving this data.</div></td></tr>').insertAfter(parentRow)
                            $(parentRow).next('tr').find('.results').slideDown('slow', function(){
                                resizeFrame('grow')
                            });
                            
                        }
                        
                    });
                    
                    /*
                     $("#dialog").dialog({
                     modal: false,
                     width: 400,
                     height: 200,
                     title: title,
                     close: function(event, ui){
                     $('#dialog >  *').remove();
                     },
                     dialogClass: 'smallDiag'
                     });
                     */
                }
                else {
                    //if not an ann or an assig, just follow the link
                    window.open(link, '_blank');
                }
                
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
            return ('');
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
            return ('');
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
    /*
     assignments will always have instructions as they are required,
     but users can just enter whitespace - that will resolve to meaningless
     markup, roundabout way of doing this!
     */
    $('#instructionHolder').html(instructions)
    $('#instructionHolder').htmlClean();
    if ($("#instructionHolder").text().length !== 0) {
        return ('<hr>' + instructions);
    }
    else {
        return ('<hr>' + langdata.noinstructions);
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


/*
 resize the iframe based on the contained document height.
 used after DOM operations that add or substract to the doc height
 */
var resizeFrame = function(updown){
    var clientH;
    if (top.location !== self.location) {
        var frame = parent.document.getElementById(window.name);
    }
    if (frame) {
        if (updown === 'shrink') {
            clientH = document.body.clientHeight;
        }
        else {
            clientH = document.body.clientHeight + 50;
        }
        $(frame).height(clientH);
    }
    else {
        // throw( "resizeFrame did not get the frame (using name=" + window.name + ")" );
    }
};

