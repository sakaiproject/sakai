/*==============================================================================

                             HTML2XHTML Converter 1.0
                             ========================
                       Copyright (c) 2004 Vyacheslav Smolin


Author:
-------
Vyacheslav Smolin (http://www.richarea.com, http://html2xhtml.richarea.com,
re@richarea.com)

About the script:
-----------------
HTML2XHTML Converter (H2X) generates a well formed XHTML string from a HTML DOM
object.

Requirements:
-------------
H2X works in  MS IE 5.0 for Windows or above,  in Netscape 7.1,  Mozilla 1.3 or
above. It should work in all Mozilla based browsers.

Usage:
------
Please see description of function get_xhtml below.

Demo:
-----
http://html2xhtml.richarea.com/, http://www.richarea.com/demo/

License:
--------
Free for non-commercial using. Please contact author for commercial licenses.


==============================================================================*/


//add \n before opening tag
var need_nl_before = '|div|p|table|tbody|tr|td|th|title|head|body|script|comment|li|meta|h1|h2|h3|h4|h5|h6|hr|ul|ol|option|';
//add \n after opening tag
var need_nl_after = '|html|head|body|p|th|style|';

var re_comment = new RegExp();
re_comment.compile("^<!--(.*)-->$");

var re_hyphen = new RegExp();
re_hyphen.compile("-$");


// Convert inner text of node to xhtml
// Call: get_xhtml(node);
//       get_xhtml(node, lang, encoding) -- to convert whole page
// other parameters are for inner usage and should be omitted
// Parameters:
// node - dom node to convert
// lang - document lang (need it if whole page converted)
// encoding - document charset (need it if whole page converted)
// need_nl - if true, add \n before a tag if it is in list need_nl_before
// inside_pre - if true, do not change content, as it is inside a <pre>
function get_xhtml(node, lang, encoding, need_nl, inside_pre) {
	var i;
	var text = '';
	var children = node.childNodes;
	var child_length = children.length;
	var tag_name;
	var do_nl = need_nl ? true : false;
	var page_mode = true;
	
	for (i = 0; i < child_length; i++) {
		var child = children[i];
		
		switch (child.nodeType) {
			case 1: { //ELEMENT_NODE
				var tag_name = String(child.tagName).toLowerCase();
				
				if (tag_name == '') break;
				
				if (tag_name == 'meta') {
					var meta_name = String(child.name).toLowerCase();
					if (meta_name == 'generator') break;
				}
				
				if (!need_nl && tag_name == 'body') { //html fragment mode
					page_mode = false;
				}
				
				if (tag_name == '!') { //COMMENT_NODE in IE 5.0/5.5
					//get comment inner text
					var parts = re_comment.exec(child.text);
					
					if (parts) {
						//the last char of the comment text must not be a hyphen
						var inner_text = parts[1];
						text += fix_comment(inner_text);
					}
				} else {
					if (tag_name == 'html') {
						text = '<?xml version="1.0" encoding="'+encoding+'"?>\n<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">\n';
					}
					
					//inset \n to make code more neat
					if (need_nl_before.indexOf('|'+tag_name+'|') != -1) {
						if ((do_nl || text != '') && !inside_pre) text += '\n';
					} else {
						do_nl = true;
					}
					
					text += '<'+tag_name;
					
					//add attributes
					var attr = child.attributes;
					var attr_length = attr.length;
					var attr_value;
					
					var attr_lang = false;
					var attr_xml_lang = false;
					var attr_xmlns = false;
					
					var is_alt_attr = false;
					
					for (j = 0; j < attr_length; j++) {
						var attr_name = attr[j].nodeName.toLowerCase();
						
						if (!attr[j].specified && 
							(attr_name != 'selected' || !child.selected) && 
							(attr_name != 'style' || child.style.cssText == '') && 
							attr_name != 'value') continue; //IE 5.0
						
						if (attr_name == '_moz_dirty' || 
							attr_name == '_moz_resizing' || 
							tag_name == 'br' && 
							attr_name == 'type' && 
							child.getAttribute('type') == '_moz') continue;
						
						var valid_attr = true;
						
						switch (attr_name) {
							case "style":
								attr_value = child.style.cssText;
								break;
							case "class":
								attr_value = child.className;
								break;
							case "http-equiv":
								attr_value = child.httpEquiv;
								break;
							case "noshade": break; //this set of choices will extend
							case "checked": break;
							case "selected": break;
							case "multiple": break;
							case "nowrap": break;
							case "disabled": break;
								attr_value = attr_name;
								break;
							default:
								try {
									attr_value = child.getAttribute(attr_name, 2);
								} catch (e) {
									valid_attr = false;
								}
								break;
						}
						
						//html tag attribs
						if (attr_name == 'lang') {
							attr_lang = true;
							attr_value = lang;
						}
						if (attr_name == 'xml:lang') {
							attr_xml_lang = true;
							attr_value = lang;
						}
						if (attr_name == 'xmlns') attr_xmlns = true;
						if (valid_attr) {
							//value attribute set to "0" is not handled correctly in Mozilla
							if (!(tag_name == 'li' && attr_name == 'value')) {
								text += ' '+attr_name+'="'+fix_attribute(attr_value)+'"';
							}
						}
						
						if (attr_name == 'alt') is_alt_attr = true;
					}
					
					if (tag_name == 'img' && !is_alt_attr) {
						text += ' alt=""';
					}
					
					if (tag_name == 'html') {
						if (!attr_lang) text += ' lang="'+lang+'"';
						if (!attr_xml_lang) text += ' xml:lang="'+lang+'"';
						if (!attr_xmlns) text += ' xmlns="http://www.w3.org/1999/xhtml"';
					}
					
					if (child.canHaveChildren || child.hasChildNodes()){
						text += '>';
//						if (need_nl_after.indexOf('|'+tag_name+'|') != -1) {
//							text += '\n';
//						}
						text += get_xhtml(child, lang, encoding, true, inside_pre || tag_name == 'pre' ? true : false);
						text += '</'+tag_name+'>';
					} else {
						if (tag_name == 'style' || tag_name == 'title' || tag_name == 'script') {
							text += '>';
							var inner_text;
							if (tag_name == 'script') {
								inner_text = child.text;
							} else {
								inner_text = child.innerHTML;
							}
							
							if (tag_name == 'style') {
								inner_text = String(inner_text).replace(/[\n]+/g,'\n');
							}
							
							text += inner_text+'</'+tag_name+'>';
						} else {
							text += ' />';
						}
					}
				}
				break;
			}
			case 3: { //TEXT_NODE
				if (!inside_pre) { //do not change text inside <pre> tag
					if (child.nodeValue != '\n') {
						text += fix_text(child.nodeValue);
					}
				} else {
					text += child.nodeValue;
				}
				break;
			}
			case 8: { //COMMENT_NODE
				text += fix_comment(child.nodeValue);
				break;
			}
			default:
				break;
		}
	}
	
	if (!need_nl && !page_mode) { //delete head and body tags from html fragment
		text = text.replace(/<\/?head>[\n]*/gi, "");
		text = text.replace(/<head \/>[\n]*/gi, "");
		text = text.replace(/<\/?body>[\n]*/gi, "");
	}
	
	return text;
}

//fix inner text of a comment
function fix_comment(text) {
	//delete double hyphens from the comment text
	text = text.replace(/--/g, "__");
	
	if(re_hyphen.exec(text)) { //last char must not be a hyphen
		text += " ";
	}
	
	return "<!--"+text+"-->";
}

//fix content of a text node
function fix_text(text) {
	//convert <,> and & to the corresponding entities
	return String(text).replace(/\n{2,}/g, "\n").replace(/\&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/\u00A0/g, "&nbsp;");
}

//fix content of attributes href, src or background
function fix_attribute(text) {
	//convert <,>, & and " to the corresponding entities
	return String(text).replace(/\&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/\"/g, "&quot;");
}
