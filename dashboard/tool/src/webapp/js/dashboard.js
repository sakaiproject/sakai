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
            'left': pos.left,
            'top': pos.top + 3
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
        
        if (itemType === "assignment" || $(this).attr('href').indexOf('assignment') !==-1) {
            //daft 2, neeed a better way of getting the entity id
            action = link.split('/')[9];            
            var assigURL = '/direct/assignment/' + action.substring(0,36) + '.json';
            jQuery.getJSON(assigURL, function(data){
                $("#dialog #metaDataMain").html('<strong>' + langdata.due + '</strong> ' + data.dueTimeString + ' (' + langdata.postedBy + data.authorLastModified + ')');
                $("#dialog #metaDataGradSub").html(resolveTypeOfGrade(data.content.typeOfGrade) + ', ' + resolveTypeOfSubmission(data.content.typeOfSubmission) + resolveMaxGradePointDisplay(data.content.maxGradePointDisplay));
                $("#dialog #description").html(resolveInstructions(data.content.instructions));
                $("#dialog #link").html('<a target ="_top" href=' + link + '>' + langdata.seemore + '</a>');
            });
            $("#dialog").dialog({
                modal: true,
                width: 400,
                height: 200,
                title: title,
                dialogClass: 'smallDiag'
            });
            
        }
        else {
            window.open(link, '_blank');
            
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
        return ('<hr>' + instructions)
    }
    else {
        return (langdata.noinstructions)
    }
    
}
var setupLang = function(){
    langdata = eval('(' + $('#lang-holder').text() + ')');
};

var setupIcons = function(){
    $('.itemLink').each(function(i){
        if($(this).closest('tr').find('.itemType').text() ==='resource'){
           $(this).addClass(getFileExtension($(this).attr('href')));     
        }
    });
    function getFileExtension(filename){
        var ext = /^.+\.([^.]+)$/.exec(filename);
        return ext == null ? "" : ext[1].toLowerCase();
    }
}  
    