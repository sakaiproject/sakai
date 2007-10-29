var validateValues = [];
var formatWidgetCount = 0;
var validateWidgetCount = 0;

function getElementsById(id){
	var result = [];	

	if(!id || typeof(id) != "string"){
		return result;
	}
	
	var ae = document.getElementsByTagName(dojo.byId(id).tagName);
	for(var i = 0; i < ae.length; i++){
		if(ae[i].id == id){
			result.push(ae[i]);
		}
	}
	return result;
}

function getString(n){
	return n && n.toString();
}

function startTest(t){
	startTestFormat(t);
	startTestValidate(t);
}

function escapeEx(s){
	var result = "";
	for(var i = 0; i < s.length; i++){
		var c = s.charAt(i);
		switch (c){
			case '"':
				result += '\\"';
				break;
			case "'":
				result += "\\'";
				break;
			default:
				result += escape(c);
				break;
		}
	}
	return result;
}

function getAllTestCases(){
	var allTestCases = [];
	for(var i = 0; i < formatWidgetCount; i++){
		allTestCases.push({
			name: "format-" + i,
			runTest: new Function("t", "startTestFormat(" + i + ", t)")
		});
	}
	for(var i = 0; i < validateWidgetCount; i++){
		allTestCases.push({
			name: "validate-" + i,
			runTest: new Function("t", "startTestValidate(" + i + ", t)")
		});
	}
	return allTestCases;
}

function startTestFormat(i, t){
	var test_node = dojo.doc.getElementById("test_display_" + i); 
	var exp = dojo.doc.getElementById("test_display_expected_" + i).value; 
	var res_node = dojo.doc.getElementById("test_display_result_" + i);
	res_node.innerHTML = test_node.value;
	res_node.style.backgroundColor = (test_node.value == exp) ? "#AFA" : "#FAA";
	res_node.innerHTML += " <a style='font-size:0.8em' href='javascript:alert(\"Expected: " + escapeEx(exp) + "\\n Result: " + escapeEx(test_node.value) + "\")'>Compare (Escaped)</a>";
	t.is(exp, test_node.value);
}

function startTestValidate(i, t){
	/*
	 * The dijit.byNode has an issue: cannot handle same id.
	 */
	var test_node = dojo.doc.getElementById("test_validate_" + i); 
	var inp_node = dojo.doc.getElementById("test_validate_input_" + i); 
	var exp = dojo.doc.getElementById("test_validate_expected_" + i).innerHTML; 
	var res_node = dojo.doc.getElementById("test_validate_result_" + i); 
	var val_node = dojo.doc.getElementById("test_display_value_" + i);
	
	test_node.value = inp_node.value;
	/*
	 * The dijit.byNode has an issue.
	 */
	var widget = null;
	var node = test_node;
	while ((widget = dijit.byNode(node)) == null){
		node = node.parentNode;
		if(!node){
			break;
		}
	}
	
	if(widget){
		widget.focus();

		var expected = validateValues[i];
		var result = widget.getValue();
		if(validateValues[i].processValue){
			expected = validateValues[i].processValue(expected);
			result = validateValues[i].processValue(result);
		}
		var parseCorrect = getString(expected) == getString(result);
		val_node.style.backgroundColor = parseCorrect ?  "#AFA" : "#FAA";
		val_node.innerHTML = getString(result) + (parseCorrect ? "" : "<br>Expected: " + getString(expected));

		res_node.innerHTML = widget.isValid && !widget.isValid() ? "Wrong" : "Correct";
		res_node.style.backgroundColor = res_node.innerHTML == exp ? "#AFA" : "#FAA";

		t.is(getString(expected), getString(result));
	} 
}

function genFormatTestCase(desc, dojoType, dojoAttrs, value, expValue, comment){
	dojo.doc.write("<tr>");
	dojo.doc.write("<td>" + desc + "</td>");
	dojo.doc.write("<td>");
	dojo.doc.write("<input id='test_display_" + formatWidgetCount + "' type='text' value='" + value + "' ");
	dojo.doc.write("dojoType='" + dojoType + "' ");
	for(var attr in dojoAttrs){
		dojo.doc.write(attr + "=\"" + dojoAttrs[attr] + "\" ");
	}
	dojo.doc.write("/>");
	dojo.doc.write("</td>");
	dojo.doc.write("<td><input id='test_display_expected_" + formatWidgetCount + "' value='" + expValue + "'></td>");
	dojo.doc.write("<td id='test_display_result_" + formatWidgetCount + "'></td>");
	dojo.doc.write("<td style='white-space:normal'>" + comment + "</td>");
	dojo.doc.write("</tr>");
	formatWidgetCount++;
}
/*
[
	{attrs: {currency: "CNY", lang: "zh-cn"}, desc: "", value:"-123456789.46", expValue: "", comment: ""},
	...
]
*/
function genFormatTestCases(title, dojoType, testCases){
	dojo.doc.write("<h2 class=testTitle>" + title + "</h2>");
	dojo.doc.write("<table border=1>");
	dojo.doc.write("<tr>");
	dojo.doc.write("<td class=title><b>Test Description</b></td>");
	dojo.doc.write("<td class=title><b>Test</b></td>");
	dojo.doc.write("<td class=title><b>Expected</b></td>");
	dojo.doc.write("<td class=title><b>Result</b></td>");
	dojo.doc.write("<td class=title><b>Comment</b></td>");
	dojo.doc.write("</tr>");
	
	for(var i = 0; i < testCases.length; i++){
		var testCase = testCases[i];
		genFormatTestCase(testCase.desc, dojoType, testCase.attrs, testCase.value, testCase.expValue, testCase.comment);
	}
	
	dojo.doc.write("</table>");
}

function genValidateTestCase(desc, dojoType, dojoAttrs, input, value, comment, isWrong){
	dojo.doc.write("<tr>");
	dojo.doc.write("<td>" + desc + "</td>");
	dojo.doc.write("<td>");
	dojo.doc.write("<input id='test_validate_" + validateWidgetCount + "' type='text' ");
	dojo.doc.write("dojoType='" + dojoType + "' ");
	for(var attr in dojoAttrs){
		dojo.doc.write(attr + "=\"" + dojoAttrs[attr] + "\" ");
	}
	dojo.doc.write("/>");
	dojo.doc.write("</td>");
	dojo.doc.write("<td><input id='test_validate_input_" + validateWidgetCount + "' value='" + input + "'></td>");
	dojo.doc.write("<td id='test_display_value_" + validateWidgetCount + "'></td>");
	dojo.doc.write("<td id='test_validate_expected_" + validateWidgetCount + "'>" + (isWrong ? "Wrong" : "Correct") + "</td>");
	dojo.doc.write("<td id='test_validate_result_" + validateWidgetCount + "'></td>");
	dojo.doc.write("<td style='white-space:normal'>" + comment + "</td>");
	dojo.doc.write("</tr>");
	validateValues.push(value);
	validateWidgetCount++;
}
/*
[
	{attrs: {currency: "CNY", lang: "zh-cn"}, desc: "", value:false, expValue: "-123456789.46", comment: ""},
	...
]
*/
function genValidateTestCases(title, dojoType, testCases){
	dojo.doc.write("<h2 class=testTitle>" + title + "</h2>");
	dojo.doc.write("<table border=1>");
	dojo.doc.write("<tr>");
	dojo.doc.write("<td class=title><b>Test Description</b></td>");
	dojo.doc.write("<td class=title><b>Test</b></td>");
	dojo.doc.write("<td class=title><b>Input</b></td>");
	dojo.doc.write("<td class=title><b>Parsed Value</b></td>");
	dojo.doc.write("<td class=title><b>Expected</b></td>");
	dojo.doc.write("<td class=title><b>Result</b></td>");
	dojo.doc.write("<td class=title><b>Comment</b></td>");
	dojo.doc.write("</tr>");
	
	for(var i = 0; i < testCases.length; i++){
		var testCase = testCases[i];
		genValidateTestCase(testCase.desc, dojoType, testCase.attrs, testCase.expValue, testCase.value, testCase.comment, testCase.isWrong);
	}
	
	dojo.doc.write("</table>");
}
