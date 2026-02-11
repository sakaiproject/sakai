/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.jsf.renderer;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.tool.assessment.util.TextFormat;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * <p> </p>
 * @author cwen@iu.edu
 * @author Ed Smiley esmiley@stanford.edu (modifications)
 * @author Joshua Ryan joshua.ryan@asu.edu (added FCKEditor)
 * @version $Id$
 */
@Slf4j
public class RichTextEditArea extends Renderer
{
  private static final ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorMessages");

  private String SITE_RESOURCE_PATH = "/access/content/group/";
  private static final String ACCESSBASE = ServerConfigurationService.getAccessUrl() + ContentHostingService.REFERENCE_ROOT;
  private static final String RELATIVEBASE = ACCESSBASE.replace(ServerConfigurationService.getServerUrl(), "");

  String editor = ServerConfigurationService.getString("wysiwyg.editor");
  
  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof org.sakaiproject.jsf.component.
            RichTextEditArea);
  }

  public void encodeBegin(FacesContext context, UIComponent component) throws
    IOException
  {
    String clientId = component.getClientId(context);

    ResponseWriter writer = context.getResponseWriter();

    Object value = null;
    String identity = (String) component.getAttributes().get("identity");
    String reset = (String) component.getAttributes().get("reset");
    if (reset == null || !reset.equals("true")) {
    	if (component instanceof UIInput)
    	{
    		value = ( (UIInput) component).getSubmittedValue();
    	}
    	if (value == null && component instanceof ValueHolder)
    	{
    		value = ( (ValueHolder) component).getValue();
    	}
    }

    boolean valueHasRichText = false;
    	if((String) value != null){
    		String valueNoNewLine = ((String) value).replaceAll("\\n", "").replaceAll("\\r", "");
    		//really simple regex to detect presence of any html tags in the value        
    		valueHasRichText = Pattern.compile(".*<.*?>.*", Pattern.CASE_INSENSITIVE).matcher(valueNoNewLine).matches();
    		//valueHasRichText = Pattern.compile(".*(<)[^\n^<]+(>).*", Pattern.CASE_INSENSITIVE).matcher((String) value).matches();
    	}
    	else {
    		value = "";
    	}
    	String hasToggle = (String) component.getAttributes().get("hasToggle");
    	
    String tmpCol = (String) component.getAttributes().get("columns");
    String tmpRow = (String) component.getAttributes().get("rows");
    String mode = (String) component.getAttributes().get("mode");
    String tmpMaxCharCount = (String) component.getAttributes().get("maxCharCount");
    
    //Get max character parameter
    int maxCharCount = 0;
    if (tmpMaxCharCount != null) 
    {
    	maxCharCount = new Integer(tmpMaxCharCount).intValue();
    }

    int col;
    int row;
    if (tmpCol != null)
    {
      col = new Integer(tmpCol).intValue();
    }
    else
    {
      col = 450;
    }
    if (tmpRow != null)
    {
      row = new Integer(tmpRow).intValue();
    }
    else
    {
      row = 80;

    }
    String outCol;
    String outRow;
    int lineOfToolBar;
    if ( (row < 80) && (col < 450))
    {
      outRow = "80";
      outCol = "450";
      lineOfToolBar = 3;
    }
    else
    if ( (row >= 80) && (col < 450))
    {
      outRow = Integer.toString(row);
      outCol = "450";
      lineOfToolBar = 3;
    }
    else
    if ( (row >= 80) && (col >= 450) && (col < 630))
    {
      outRow = Integer.toString(row);
      outCol = Integer.toString(col);
      lineOfToolBar = 3;
    }
    else
    if ( (row >= 80) && (col >= 630))
    {
      outRow = Integer.toString(row);
      outCol = Integer.toString(col);
      lineOfToolBar = 2;
    }
    else
    if ( (row < 80) && (col >= 630))
    {
      outRow = "80";
      outCol = Integer.toString(col);
      lineOfToolBar = 2;
    }
    else
    {
      outRow = "80";
      outCol = Integer.valueOf(col).toString();
      lineOfToolBar = 3;
    }

    String justArea = (String) component.getAttributes().get("justArea");

      if (tmpCol == null) {
    	  encodeCK(writer, (String) value, identity, outCol, 
              outRow, justArea, clientId, valueHasRichText, hasToggle, true, mode, maxCharCount);
      }
      else {
    	  encodeCK(writer, (String) value, identity, outCol, 
                  outRow, justArea, clientId, valueHasRichText, hasToggle, false, mode, maxCharCount);
      }
  }


  private void encodeCK(ResponseWriter writer, String value, String identity, String outCol, 
         String outRow, String justArea, String clientId, boolean valueHasRichText, String hasToggle, boolean columnsNotDefined, String mode, int maxCharCount) throws IOException {

    String componentId = StringUtils.isBlank(identity) ? clientId : clientId.contains(":") ? clientId.substring(0, clientId.indexOf(":") + 1) + identity : identity;

    //If no specific mode is set, it's delivery by default
	boolean disableWysiwyg = false;
    if (mode == null) {
      //Disable wysiwyg in delivery based on this property
      disableWysiwyg = ServerConfigurationService.getBoolean("samigo.wysiwyg.delivery.disable",false);
    }
    //Maybe do something special for the other modes
    else {
        if ("author".equals(mode)) {
          log.debug("author mode wysiwyg");
        }
    }
	  String samigoFrameId = "Main";
	  if (org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement() != null) {
		samigoFrameId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId().replace("-","x");
	  }

	  //come up w/ rows/cols for the textarea if needed
	  int textBoxRows = (new Integer(outRow).intValue()/20);
	  int textBoxCols = 0;
	  if (columnsNotDefined) {
		  textBoxCols  = (new Integer(outRow).intValue()/3);
	  }
	  else {
		  textBoxCols = (new Integer(outCol).intValue()/4);
	  }

    //fck's tool bar can get pretty big
    if (new Integer(outRow).intValue() < 300) 
    {
         outRow = (new Integer(outRow).intValue() + 100) + "";
    }

    //figure out if the toggle should be on
    boolean shouldToggle = ( (hasToggle != null) && (hasToggle.equals("yes")) && !valueHasRichText);
    
    if(shouldToggle && !disableWysiwyg)
    {    	
    	String show_editor = rb.getString("show_editor");
    	writer.write("<div class=\"toggle_link_container\"><a class=\"toggle_link\" id=\"" +componentId+ "_toggle\" href=\"javascript:show_editor('" + componentId + "', '" + samigoFrameId + "', " + maxCharCount + ");\">" + show_editor + "</a></div>\n");
    }
    else {
        	value = ComponentManager.get(FormattedText.class).escapeHtmlFormattedTextarea((String) value);
    }

    String inputTextArea = "<textarea name='%s' id='%s' rows='%d' cols='%d' class='%s'>";
    writer.write(String.format(inputTextArea, clientId + "_textinput", componentId + "_textinput", textBoxRows, textBoxCols, "simple_text_area"));
    writer.write((String) value);
    writer.write("</textarea>");
    if (!disableWysiwyg) 
    	if (shouldToggle) {
    		writer.write("<input type=\"hidden\" name=\"" + clientId + "_textinput_current_status\" id=\"" + componentId + "_textinput_current_status\" value=\"firsttime\">");
    	}
    	else if(hasToggle.equals("plain") && !valueHasRichText){
        	writer.write("<input type=\"hidden\" name=\"" + clientId + "_textinput_current_status\" id=\"" + componentId + "_textinput_current_status\" value=\"fckonly\" data-first=\"" + componentId + "\" data-second=\"" + samigoFrameId + "\">");
        }
    	else {
    		writer.write("<input type=\"hidden\" name=\"" + clientId + "_textinput_current_status\" id=\"" + componentId + "_textinput_current_status\" value=\"fckonly\">");
    	}
	//if toggling is off or the content is already rich, make the editor show up immediately if hasToggle is not plain
	if(!shouldToggle && (!hasToggle.equals("plain") || valueHasRichText)){
		writer.write("<script type=\"text/javascript\" defer=\"1\">chef_setupformattedtextarea('" + componentId + "', false, '" + samigoFrameId +"', " + maxCharCount + ");</script>");
    }
  }

  public void decode(FacesContext context, UIComponent component) {
    if (null == context || null == component ||
        ! (component instanceof org.sakaiproject.jsf.component.RichTextEditArea))
    {
      throw new IllegalArgumentException();
    }

    String clientId = component.getClientId(context);
    String identity = (String) component.getAttributes().get("identity");
    String componentId = StringUtils.isBlank(identity) ? clientId : clientId.contains(":") ? clientId.substring(0, clientId.indexOf(":") + 1) + identity : identity;

    Map requestParameterMap = context.getExternalContext().getRequestParameterMap();

    String newValue = (String) requestParameterMap.get(clientId + "_textinput");
    String current_status = (String) requestParameterMap.get(clientId + "_textinput_current_status");
    String finalValue = newValue;
    
    // if use hid the FCK editor, we treat it as text editor
	if ("firsttime".equals(current_status)) {
		finalValue = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(newValue);
	}
	else {
        finalValue = ComponentManager.get(FormattedText.class).processFormattedText(newValue, null, null);
	}

	if (StringUtils.startsWithAny(componentId, "assessmentSettingsAction:", "modifyPartForm:", "itemForm:")) {
		appendSecurePropertyToContent(finalValue);
	}

    org.sakaiproject.jsf.component.RichTextEditArea comp = (org.sakaiproject.jsf.component.RichTextEditArea) component;
    comp.setSubmittedValue(finalValue);
  }

  private void appendSecurePropertyToContent(String msgBody) {
	if(StringUtils.isNotEmpty(msgBody)){
		Document doc = Jsoup.parse(msgBody);

		Elements links = doc.select("a[href]");
		Elements media = doc.select("[src]");
		List<String> references = new ArrayList<String>();
		// href ...
		for (Element link : links) {
			references.add(link.attr("abs:href"));
		}

		// img ...
		for (Element src : media) {
			references.add(src.attr("abs:src"));
		}

		for (String reference : references){
			if (reference.contains(SITE_RESOURCE_PATH)) {
				String imgId = "";
				if ((reference.startsWith(ACCESSBASE)) || (reference.startsWith(RELATIVEBASE))) {
					imgId = reference.replaceFirst(ACCESSBASE, "").replaceFirst(RELATIVEBASE, "");

					try {
						imgId = URLDecoder.decode(imgId);
						log.debug("appendSecurePropertyToContent looking for img {} ", imgId);
						ContentResourceEdit resourceEdit = ContentHostingService.editResource(imgId);
						if (resourceEdit == null) {
							log.warn("Resource not found for id {}", imgId);
							continue;
						}
						ResourcePropertiesEdit props = resourceEdit.getPropertiesEdit();
						props.addProperty(ResourcePropertiesEdit.PROP_SECURED, Boolean.TRUE.toString());
						ContentHostingService.commitResource(resourceEdit, NotificationService.NOTI_NONE);
					} catch (Exception e) {
						log.warn("Exception from ContentHostingService for resource {} : {}", imgId, e);
					}
				}
			}
		}
	}
  }
}
