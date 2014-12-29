//this is to resolve an IE bug, aaaargh we are still in the dark ages
if(typeof String.prototype.trim !== 'function') {
	String.prototype.trim = function() {
		return this.replace(/^\s+|\s+$/g, ''); 
	}
}

//jQuery functions
if (typeof $ === 'undefined') {
	$ = jQuery;
}
//------------------------------------------//
//	The ready function, setup the form		//
//	for all the events						//
//------------------------------------------//
$(document).ready(function(){
	var inReadyCall = true;
	
	//only applies to EMI authoring
	//this value is set in extendedMatchingItems.jsp
	if (!emiAuthoring) return;
	
	//------------------------------------------//
	//	Global variables						//
	//	Variables used in the emi functions		//
	//------------------------------------------//
	var highestOptionId = +25;
	var highestItemId = +59;
	var maxAvailableItems = +30;
	var minOptions = +2;
	var currentOptions = +$("select[id='itemForm:answerOptionsRichCount']").val();
	var optionsAtStart = +8;
	var itemsAtStart = +4;
	var removeLabel = "X";
	var ANSWER_OPTION_LABELS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	//----------------------------------------------//
	//	VALIDATION									//
	//	set the validation variables from hidden	//
	//	fields in the extendedMatchingItems.jsp		//
	//----------------------------------------------//
	var answer_point_value_error = $("input[id='answer_point_value_error']").val();
	var theme_text_error = $("input[id='theme_text_error']").val();
	var number_of_rich_text_options_error = $("input[id='number_of_rich_text_options_error']").val();
	var blank_or_non_integer_item_sequence_error = $("input[id='blank_or_non_integer_item_sequence_error']").val();
	var correct_option_labels_error = $("input[id='correct_option_labels_error']").val();
	var correct_option_labels_invalid_error = $("input[id='correct_option_labels_invalid_error']").val();
	var at_least_two_options_required_error = $("input[id='at_least_two_options_required_error']").val();
	var at_least_two_pasted_options_required_error = $("input[id='at_least_two_pasted_options_required_error']").val();
	
	//----------------------------------------------//
	//	Create error message tags					//
	//	Setup the table at the top of 				//
	//	extendedMatchingItems.jsp to hold the error	//
	//	message. This replicate the way the errors	//
	//	are shown in the normal jsf submits			//
	//----------------------------------------------//
	var $errorMessageTable = $('#emiErrorMessageTable');
	var $tableRowElement = $('<tr></tr>');
	var $tableColumnElement = $('<td></td>');
	
	//----------------------------------------------//
	//	Validation function on save					//
	//----------------------------------------------//
	$("input[value='Save']").bind('click', function(){
		updateAnswerPointScore();
		$errorMessageTable.removeClass('messageSamigo');
		$('#emiErrorMessageTable > tr').remove();
		var errorMessages = new Array();
		var errorNumber=+0;

		//Validate Answer Point Value
		var answerPointValue = $("input[name='itemForm:answerptr']").val();
		if (answerPointValue.trim()=="" || /[^0-9.]/g.test(answerPointValue)) {
			errorMessages[errorNumber++] = answer_point_value_error;
		}
		
		//Validate Theme Text
		var themeText = $("input[name='itemForm:themetext']").val();
		if (themeText.trim()=="") {
			errorMessages[errorNumber++] = theme_text_error;
		}
		
		//Validate Options
		var simpleOrRichAnswerOptions = $("input[name='itemForm:emiAnswerOptionsSimpleOrRich']:checked").val();
		var optionLabels = "";
		
		if (simpleOrRichAnswerOptions==0) { //simple text options
			for (j=0; j<highestOptionId; j++) {
				var optionText = $("input[id='itemForm:emiAnswerOptions:" + j + ":Text']");
				if (optionText.is(':visible') && (optionText.val()==null || optionText.val().trim()=="") ) {
					//ignore
					//break;
				}
				else if (optionText.is(':visible')) {
					var label = ANSWER_OPTION_LABELS.substring(j, j+1);
					optionLabels+=label;
				}else{
					//not visible
					break;
				}
			}
			if (currentOptions < minOptions) {
				errorMessages[errorNumber++] = at_least_two_options_required_error;
			}
		}
		else if(simpleOrRichAnswerOptions==2){//simple text paste
			var pastedOptions = $("textarea[id='itemForm:emiAnswerOptionsPaste']").val();
			if (pastedOptions == null || pastedOptions.trim()=="") {
				errorMessages[errorNumber++] = at_least_two_pasted_options_required_error;
			}
			else if (pastedOptions.split("\n").length < minOptions) {
				errorMessages[errorNumber++] = at_least_two_pasted_options_required_error;
			}else{
				currentOptions = pastedOptions.split("\n").length;
				optionLabels = ANSWER_OPTION_LABELS.substring(0, currentOptions);
			}
		}
		else { // Rich
			var richAnswerOptionsCount = +$("select[id='itemForm:answerOptionsRichCount']").val();
			if (richAnswerOptionsCount == 0) {
				errorMessages[errorNumber++] = number_of_rich_text_options_error;
			}
			else {
				currentOptions = richAnswerOptionsCount;
				optionLabels = ANSWER_OPTION_LABELS.substring(0, richAnswerOptionsCount);
			}
		}
		
		//Validate Items
		for (j=0; j<=highestItemId; j++) {
			var labelInput = $("input[id='itemForm:emiQuestionAnswerCombinations:" + j + ":Label']");
			var itemText = $("textarea[id^='itemForm:emiQuestionAnswerCombinations:" + j +":']").val();
			var hasAttachment = $("input[id='itemForm:emiQuestionAnswerCombinations:" + j + ":hasAttachment']");
			var row = $("table[id='itemForm:emiQuestionAnswerCombinations:" + i + ":Row']");

			if (row && row.is(':visible') && labelInput.val() !== removeLabel 
					&& ((itemText && itemText.trim()!=="") || hasAttachment.val()==="true") )  {
				if (labelInput.val().trim()=="" || /[^0-9]/g.test(labelInput.val())) {
					errorMessages[errorNumber++] = blank_or_non_integer_item_sequence_error + labelInput.val();
				}
				
				var correctOptionLabels = $("input[id='itemForm:emiQuestionAnswerCombinations:" + j + ":correctOptionLabels']").val().toUpperCase();
				if (correctOptionLabels.trim()=="" || /[^A-Z,]/gi.test(correctOptionLabels)) {
					errorMessages[errorNumber++] = correct_option_labels_error + labelInput.val();
				}
				
				if (optionLabels.length > 0) {
					for (i=0; i<correctOptionLabels.length; i++) {
						thisLabel = correctOptionLabels.substring(i, i+1);
						if (optionLabels.indexOf(thisLabel)==-1) {
							errorMessages[errorNumber++] = correct_option_labels_invalid_error + labelInput.val();
							break;
						}
					}
				}
			}else{
				break;
			}
		}
		
		if (errorNumber > 0) {
			for (i=0; i<errorNumber; i++) {
				var col = $tableColumnElement.clone().append(errorMessages[i]);
				var row = $tableRowElement.clone().appendTo($errorMessageTable);
				col.appendTo(row);
			}
			$errorMessageTable.addClass('messageSamigo');
			top.window.scrollTo(0,0);
			return false;
		}
		else {
			return true;
		}
	});
	
	//----------------------------------------------//
	//  ************* OPTIONS ********************  //
	//----------------------------------------------//
	var emiOptionAddLink = $("a[id='itemForm:addEmiAnswerOptionsLink']");
	var addEmiAnswerOptionsSelect = $("select[id='itemForm:addEmiAnswerOptionsSelect']");
	
	//----------------------------------------------//
	//	Show/Hide Simple or Rich-Text Options		//
	//	based on user selection 					//
	//----------------------------------------------//
	var $emiAnswerOptionsSimpleOptions = $("#emiAnswerOptionsSimpleOptions");
	var $emiAnswerOptionsSimplePaste = $("#emiAnswerOptionsSimplePaste");
	var $emiAnswerOptionsRich = $("#emiAnswerOptionsRich");
	$("input[name='itemForm:emiAnswerOptionsSimpleOrRich']").bind('click', function(){
		if (this.value === "0"){
			//show simple option
			$emiAnswerOptionsSimpleOptions.show();
			$emiAnswerOptionsSimplePaste.hide();
			$emiAnswerOptionsRich.hide();
			//update currentOptions
			updateSimpleTextOptionCount();
		}else if (this.value === "1"){
			//show rich
			$emiAnswerOptionsSimpleOptions.hide();
			$emiAnswerOptionsSimplePaste.hide();
			$emiAnswerOptionsRich.show();
			currentOptions = $("select[id='itemForm:answerOptionsRichCount']").val();
		}else if (this.value === "2"){
			//show simple paste
			$emiAnswerOptionsSimpleOptions.hide();
			$emiAnswerOptionsSimplePaste.show();
			$emiAnswerOptionsRich.hide();
			//set currentOptions
			$("textarea[id='itemForm:emiAnswerOptionsPaste']").trigger('change');
		}
	});
	$("select[id='itemForm:answerOptionsRichCount']").bind('change', function(){
		currentOptions = $("select[id='itemForm:answerOptionsRichCount']").val();
	});
	//trigger startup events
	var radioSimpleOrRichChecked = $("input[name='itemForm:emiAnswerOptionsSimpleOrRich']:checked");
	radioSimpleOrRichChecked.trigger('click');
	
	//--------------------------------------//
	//	Hide excess Options at start		//
	//--------------------------------------//
	if(radioSimpleOrRichChecked.value === "0"){
        updateSimpleTextOptionCount();
    }
	
	//------------------------------------------//
	//	Add OptionLabels for Paste Options		//
	//------------------------------------------//
	var $labelsTable = $("#emiAnswerOptionsPasteLabelsTable");
	$("textarea[id='itemForm:emiAnswerOptionsPaste']").change(function(){
		var pastedOptions = $(this).val();
		$('#emiAnswerOptionsPasteLabelsTable > tr').remove();
		currentOptions = 0;
		//VULA-1887: Simple text - for pasting options workflow error - leading to broken functionality
		if (pastedOptions == null || pastedOptions.trim() == "") { 
			return;
		}
		var optionsArray = pastedOptions.split("\n");
		for(i = 0; i < optionsArray.length; i++){
			if (optionsArray[i] != null && optionsArray[i].trim()!="") {
				var col = $tableColumnElement.clone().append(
						ANSWER_OPTION_LABELS.substring(i, i+1) + '.) ' + optionsArray[i]);
				var row = $tableRowElement.clone().appendTo($labelsTable);
				col.appendTo(row);
				currentOptions++;
			}
		}
	});
	
	//----------------------------------//
	//	Remove Option			 		//
	//									//
	//----------------------------------//
	for (i=0; i<=highestOptionId; i++) {
		var emiOptionRemoveLink = $("a[id='itemForm:emiAnswerOptions:" + i + ":RemoveLink']");
		emiOptionRemoveLink.bind('click', function() {
			var optionId = +($(this).attr("id").split(":")[2]);
			for (j=optionId; j<highestOptionId; j++) {
				var k = +j+1;
				var optionText1 = $("input[id='itemForm:emiAnswerOptions:" + j + ":Text']");
				var optionText2 = $("input[id='itemForm:emiAnswerOptions:" + k + ":Text']");
				optionText1.val(optionText2.val());
				//if reached the visible-invisible boundary, hide the last visible row
				if (optionText1.is(':visible') && optionText2.is(':hidden')) {
					optionText1.val("");
					$("table[id='itemForm:emiAnswerOptions:" + j + ":Row']").parent().parent().hide();
					break;
				}
			}
			var lastOptionText = $("input[id='itemForm:emiAnswerOptions:" + highestOptionId + ":Text']");
			lastOptionText.val("");
			$("table[id='itemForm:emiAnswerOptions:" + highestOptionId + ":Row']").parent().parent().hide();
			setContainerHeight();
			currentOptions--;
			updateAddEmiAnswerOptionsCount();
			return false;
	    });
	}
	
	//----------------------------------//
	//	Add Option			 			//
	//									//
	//----------------------------------//
    if(emiOptionAddLink){
        emiOptionAddLink.bind('click', function(event){
            var addCount = parseInt(addEmiAnswerOptionsSelect.val());
            for (i=currentOptions; i<currentOptions+addCount; i++) {
                $("table[id='itemForm:emiAnswerOptions:" + i + ":Row']").parent().parent().show();
            }
            setContainerHeight();
            currentOptions += addCount;
            updateAddEmiAnswerOptionsCount();
            event.preventDefault();
            return false;
        });
    }
	
	//--------------------------------------//
	//	updateSimpleTextOptionCount			//
	//	Hide excess Options and update		//
	//	the currentOptions value			//
	//--------------------------------------//
	function updateSimpleTextOptionCount(){
		var isAllNull = true;
		currentOptions = highestOptionId + 1;
		for (i=highestOptionId; i>=0; i--) {
			var optionText = $("input[id='itemForm:emiAnswerOptions:" + i + ":Text']");
			if (optionText.val() === "" || optionText.val() === null) {
				$("table[id='itemForm:emiAnswerOptions:" + i + ":Row']").parent().parent().hide();
				currentOptions--;
			}
			else {
				isAllNull = false;
				break;
			}
		}
		if (isAllNull) {
			for (i=0; i<optionsAtStart; i++) {
				$("table[id='itemForm:emiAnswerOptions:" + i + ":Row']").parent().parent().show();
				currentOptions++;
			}
		}
		updateAddEmiAnswerOptionsCount();
	}
	
	//------------------------------------------//
	//	updateAddEmiAnswerOptionsCount			//
	//	This function will update the 			//
	//	addEmiAnswerOptionsSelect with the 		//
	// correct number of options				//
	//------------------------------------------//
	function updateAddEmiAnswerOptionsCount(){
		var count = highestOptionId-currentOptions +1;

		if (currentOptions==(highestOptionId+1)) {
			emiOptionAddLink.hide();
			addEmiAnswerOptionsSelect.hide();
		}else{
			emiOptionAddLink.show();
			addEmiAnswerOptionsSelect.show();
		}
		
		addEmiAnswerOptionsSelect.empty();
		for (j=1; j<=count; j++) {
			if (j == 1) {
				addEmiAnswerOptionsSelect.append('<option selected="selected" value="'+ j +'">'+ j +'</option>');
			}
			else {
				addEmiAnswerOptionsSelect.append('<option value="'+ j +'">'+ j +'</option>');
			}
		}
	}
	
	//----------------------------------------------//
	//  ******** Lead in Statement ***************  //
	//----------------------------------------------//
	//------------------------------------------//
	//	Lead in Statement						//
	//	set the lead in default	and manage the	//
	//	switch between user input and default	//
	//------------------------------------------//
	var leadinStatementDescription = $('#default_lead_in_statement_description').val().replace("<br/>", "\n");
	var leadinStatementField = $('[identity="lead_in_statement"]');
	leadinStatementField.focus(function() {
		var leadin = $(this);
		if (leadin.val() == leadinStatementDescription) {
			leadin.val('');
			leadin.removeClass('placeholder');
		}
	}).blur(function() {
		var leadin = $(this);
		if (leadin.val() == '' || leadin.val() == leadinStatementDescription) {
			leadin.addClass('placeholder');
			leadin.val(leadinStatementDescription);
		}
	}).blur();
	//set the default part on submit
	leadinStatementField.parents('form').submit(function() {
		leadinStatementField.each(function() {
			var leadin = $(this);
			if (leadin.val() == leadinStatementDescription) {
				leadin.val($('#default_lead_in_statement').val());
			}
		})
	});
	
	//----------------------------------------------//
	//  ***************** Items ******************  //
	//----------------------------------------------//
	var emiItemAddLink = $("a[id='itemForm:addEmiQuestionAnswerCombinationsLink']");
	var addEmiItemSelect = $("select[id='itemForm:addEmiQuestionAnswerCombinationsSelect']");
	
	//----------------------------------//
	//	Hide excess Items at start		//
	//----------------------------------//
	var emiVisibleItems = $("input[id='itemForm:emiVisibleItems']");
	var availibleItems = +0;
	var isAllNull = true;
	for (i=highestItemId; i>=0; i--) {
		var itemText = $("textarea[id^='itemForm:emiQuestionAnswerCombinations:" + i +"']");
		var hasAttachment = $("input[id='itemForm:emiQuestionAnswerCombinations:" + i + ":hasAttachment']");
		var labelInput = $("input[id='itemForm:emiQuestionAnswerCombinations:" + i + ":Label']");
		if ((itemText.val() === "" && hasAttachment.val()==="false") || labelInput.val()==removeLabel) {
			$("table[id='itemForm:emiQuestionAnswerCombinations:" + i + ":Row']").parent().parent().hide();
		}
		else {
			isAllNull = false;
		}
		if(labelInput.val()!=removeLabel){
			availibleItems++;
		}
	}
	var itemsToShow=+0;
	if (isAllNull) {
		itemsToShow = +itemsAtStart;
		emiVisibleItems.val(itemsToShow);
	}
	else {
		itemsToShow = +emiVisibleItems.val();
	}
	availibleItems -= itemsToShow;
	for (i=0; i<=highestItemId; i++) {
		if (+itemsToShow == +0) break;
		var labelInput = $("input[id='itemForm:emiQuestionAnswerCombinations:" + i + ":Label']");
		if (labelInput.val() != removeLabel) {
			$("table[id='itemForm:emiQuestionAnswerCombinations:" + i + ":Row']").parent().parent().show();
			if (labelInput.val()==itemsToShow) {
				break;
			}
		}
	}
	updateAddEmiItemsCount(+emiVisibleItems.val(), availibleItems, highestItemId+1);
	
	//----------------------------------//
	//	Remove Items			 		//
	//									//
	//----------------------------------//
	for (i=0; i<=highestItemId; i++) {
		var emiItemRemoveLink = $("a[id='itemForm:emiQuestionAnswerCombinations:" + i + ":RemoveLink']");
		emiItemRemoveLink.bind('click', function() {
			var itemId = +($(this).attr("id").split(":")[2]);
			var row = $("table[id='itemForm:emiQuestionAnswerCombinations:" + itemId + ":Row']").parent().parent();
			var labelRemove = $("input[id='itemForm:emiQuestionAnswerCombinations:" + itemId + ":Label']");
			labelRemove.val(removeLabel);
			row.hide();
			var seq=+0;
			availibleItems=+0;
			//Resequence items
			for (j=0; j<=highestItemId; j++) {
				row = $("table[id='itemForm:emiQuestionAnswerCombinations:" + j + ":Row']");
				var labelInput = $("input[id='itemForm:emiQuestionAnswerCombinations:" + j + ":Label']");
				if (row && row.is(':visible') && labelInput.val() !== removeLabel)  {
					seq++;
					labelInput.val(seq);
					labelInput.trigger('change');
				}else if(labelInput.val() !== removeLabel){
					availibleItems++;
				}
			}
			emiVisibleItems.val(seq);
			setContainerHeight();
			updateAddEmiItemsCount(+emiVisibleItems.val(), availibleItems, highestItemId+1);
			return false;
	    });
	}
	
	//----------------------------------//
	//	Add Items			 			//
	//									//
	//----------------------------------//
    if(emiItemAddLink){
        emiItemAddLink.bind('click', function(){
            var j=+0;
            var itemCount=+0;
            var totalCount=+0;
            availibleItems = +0;
            var addCount = parseInt(addEmiItemSelect.val());

            for (i=0; i<=highestItemId; i++) {
                var row = $("table[id='itemForm:emiQuestionAnswerCombinations:" + i + ":Row']");
                var labelInput = $("input[id='itemForm:emiQuestionAnswerCombinations:" + i + ":Label']");
                if (row && !row.is(':visible') && labelInput.val() !== removeLabel) {
                    if(addCount != 0){
                        itemCount++;
                        labelInput.val(itemCount);
                        labelInput.trigger('change');
                        emiVisibleItems.val(itemCount);
                        row.parent().parent().show();
                        addCount--;
                    }else{
                        availibleItems++;
                    }
                }
                else if (row && row.is(':visible')) {
                    itemCount++;
                    labelInput.val(itemCount);
                    labelInput.trigger('change');
                    emiVisibleItems.val(itemCount);
                }
                if(row){
                    totalCount++;
                }
            }
            setContainerHeight();
            updateAddEmiItemsCount(itemCount, availibleItems, totalCount);
            return false;
        });
    }
	
	//------------------------------------------//
	//	updateAddEmiItemsCount					//
	//	This function will update the 			//
	//	addEmiQuestionAnswerCombinationsSelect	//
	//	with the correct number of items		//
	//------------------------------------------//
	//var itemCountParam = $("")
	function updateAddEmiItemsCount(itemCount, availibleItems, totalCount){
		var currentItems = +emiVisibleItems.val();
		if(!availibleItems){
			availibleItems = highestItemId - currentItems + 1;
		}

		if (availibleItems==0) {
			emiItemAddLink.hide();
			addEmiItemSelect.hide();
		}else{
			emiItemAddLink.show();
			addEmiItemSelect.show();
		}
		
		addEmiItemSelect.empty();
		for (j=1; j<=availibleItems; j++) {
			if (j == 1) {
				addEmiItemSelect.append('<option selected="selected" value="'+ j +'">'+ j +'</option>');
			}
			else {
				addEmiItemSelect.append('<option value="'+ j +'">'+ j +'</option>');
			}
		}
	}
	
	//--------------------------------------------------------------//
	//	CorrectOptionsLabels, RequiredOptionsCount and ItemPoints	//
	//	This looks at the correctOptionLabels and then set the		//
	//	number of select options on the requiredOptionsCount		//
	//	select.														//
	//	It also looks at the requiredOptionsCount and update the	//
	//	item point value.											//
	//	Also add a validator to the CorrectOptionsLabels fields 	//
	//	so only valid options can be entered						//
	//--------------------------------------------------------------//
	var all_option = $("input[id=all]").val();
	for (i=0; i<=highestItemId; i++) {
		var emiCorrectOptionLabelsInput = $("input[id='itemForm:emiQuestionAnswerCombinations:" + i + ":correctOptionLabels']");
		var requiredOptionsCountSelect = $("select[id='itemForm:emiQuestionAnswerCombinations:" + i + ":requiredOptionsCount']");
		var itemScoreInput = $("input[id='itemForm:emiQuestionAnswerCombinations:" + i + ":itemScore']");
		if (emiCorrectOptionLabelsInput==null) break;
		//update required select
		emiCorrectOptionLabelsInput.bind('change', function() {
			var itemId = +($(this).attr("id").split(":")[2]);
			requiredOptionsCountSelect = $("select[id='itemForm:emiQuestionAnswerCombinations:" + itemId + ":requiredOptionsCount']");
			var currentSelection = requiredOptionsCountSelect.val();
			
			var correctOpts = $(this).val().toUpperCase();
			var maxOptions = +0;
			for (var iMax=0; iMax<correctOpts.length; iMax++) {
				var currCorrectLabel = correctOpts.substring(iMax, iMax+1);
				if (ANSWER_OPTION_LABELS.indexOf(currCorrectLabel) != -1) {
					maxOptions += 1;
				}
			}
			
			requiredOptionsCountSelect.empty();
			if(maxOptions == 0){
				requiredOptionsCountSelect.append('<option value="0">' + all_option + '</option>');
			}else{
			for (j=1; j<=maxOptions; j++) {//Note: here was a +3, not sure why?
				var optionTag = '<option';
				if (j == currentSelection) {
					optionTag = optionTag + ' selected="selected"';
				}
				optionTag = optionTag + ' value="';
				if(j == maxOptions){
					optionTag = optionTag + '0"';
					if(currentSelection == 0){
						optionTag = optionTag + ' selected="selected"';
					}
				}else{
					optionTag = optionTag + j + '"';
				}
				optionTag = optionTag + '>';
				if(j == maxOptions){
					optionTag = optionTag + all_option + '(' + j + ')';
				}else{
					optionTag = optionTag + j;
				}
				optionTag = optionTag + '</option>';
				requiredOptionsCountSelect.append(optionTag);
			}
			}
			//set score
			if(currentSelection == 0 || currentSelection > maxOptions){
				setItemScore(itemId, maxOptions);
			}else{
				setItemScore(itemId, currentSelection);
			}
			return false;
	    });
		emiCorrectOptionLabelsInput.trigger('change');
		//update item point value
		requiredOptionsCountSelect.bind('change', function() {
			var itemId = +($(this).attr("id").split(":")[2]);
			if($(this).val() == 0){
				setItemScore(itemId, $("input[id='itemForm:emiQuestionAnswerCombinations:" + itemId + ":correctOptionLabels']").val().length);
			}else{
				setItemScore(itemId, $(this).val());
			}
		});
		//update overall score
		itemScoreInput.keyup(function(event) {
			updateAnswerPointScore();
		});
		itemScoreInput.keypress(function(event){
			var itemId = +($(this).attr("id").split(":")[2]);
			$("input[id='itemForm:emiQuestionAnswerCombinations:" + itemId + ":itemScoreUserSet']").val(true);
		});
		
		//------------------------------------------//
		//	Correct Option Labels					//
		//	This does frontend validation for the	//
		//	correct options labels entered			//
		//------------------------------------------//
		emiCorrectOptionLabelsInput.keypress(function(event) {
			return checkEMIOptions($(this).context, ANSWER_OPTION_LABELS.substring(0,currentOptions), event);
		});
	}
	
	function setItemScore(itemId, score){
		var itemScoreUserSet = $("input[id='itemForm:emiQuestionAnswerCombinations:" + itemId + ":itemScoreUserSet']").val();
		var itemScoreInput = $("input[id='itemForm:emiQuestionAnswerCombinations:" + itemId + ":itemScore']");
		if(itemScoreUserSet === "true"){
			//don't override value, use the user value
			score = itemScoreInput.val();
			// trim ".0" from the end
                        if(score.length > 2 && (score.lastIndexOf("0") == score.length-1 && score.lastIndexOf(".") == score.length-2)){
                                score = score.substring(0, score.length-2);
                        }

		}
		itemScoreInput.val(score);
		updateAnswerPointScore();
	}
	
	function updateAnswerPointScore(){
		var total = 0;
		for (j=0; j<=highestItemId; j++) {
			var row = $("table[id='itemForm:emiQuestionAnswerCombinations:" + j + ":Row']");
			if (row && row.is(':visible')){
				total += parseFloat($("input[id='itemForm:emiQuestionAnswerCombinations:" + j + ":itemScore']").val());
			}
		}
		if(!inReadyCall){
			$("input[name='itemForm:answerptr']").val(total);
		}
	}
	
	//--------------------------------------//
	//	Answer Combination Labels			//
	//	Updates the span showning the		//
	//	Answer Combination Label			//
	//--------------------------------------//
	for (j=0; j<=highestItemId; j++) {
		var labelInput = $("input[id='itemForm:emiQuestionAnswerCombinations:" + j + ":Label']");
		labelInput.bind('change', function() {
			$(this).parent().children("span[id='showItemLabel']").html($(this).val());
		});
		labelInput.trigger('change');
	}

	//Make the container visible after all processing
	$("div[id=portletContent]").css('display','block');
	
	function setContainerHeight() {
		var containerFrame = $("iframe[class='portletMainIframe']", parent.document.body);
		containerFrame.height($(document.body).height() + 30);
	}
	inReadyCall = false;
});
