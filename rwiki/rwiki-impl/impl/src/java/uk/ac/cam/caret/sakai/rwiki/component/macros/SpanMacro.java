package uk.ac.cam.caret.sakai.rwiki.component.macros;

import java.io.IOException;
import java.io.Writer;

import org.radeox.macro.BaseMacro;
import org.radeox.macro.parameter.MacroParameter;

public class SpanMacro extends BaseMacro {
    private static String[] paramDescription = {
            "1,class: The class to assign to this block.",
            "id: An id to assign to this block.",
            "anchor: An anchor to assign to this block"
            };

    private static String description = "Places a span around a section of rwiki rendered content.";

    public String[] getParamDescription() {
        return paramDescription;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.radeox.macro.Macro#getDescription()
     */
    public String getDescription() {
        return description;
    }

    public String getName() {
        return "span";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.radeox.macro.Macro#execute(java.io.Writer,
     *      org.radeox.macro.parameter.MacroParameter)
     */
    public void execute(Writer writer, MacroParameter params)
            throws IllegalArgumentException, IOException {
        
        String cssClass = params.get("class");
        if (cssClass == null) {
            cssClass = params.get(0);
            if (cssClass.startsWith("id=") ) {
                cssClass = null;
            } else if (cssClass.startsWith("name=")) {
                cssClass = null;
            }
        }
        String id = params.get("id");
        
        String anchorName = params.get("name");
        
        writer.write("<span");
        if (cssClass != null && !cssClass.equals("")) {
            writer.write(" class='");
            writer.write(cssClass.replaceAll("'","&apos;"));
            writer.write('\'');
        }
        if (id != null && !id.equals("")) {
            writer.write(" id='");
            char[] nameChars = id.toCharArray();
            int end = 0;
            for (int i = 0; i < nameChars.length; i++) {
                if (Character.isLetterOrDigit(nameChars[i])) {
                    nameChars[end++] = nameChars[i]; 
                }
            }
            if (end > 0) { 
                writer.write(nameChars, 0, end);
            }
            writer.write('\'');
        }
        writer.write('>');
        if (anchorName != null && !anchorName.equals("")) {
            writer.write("<a name=\"");
            char[] nameChars = anchorName.toCharArray();
            int end = 0;
            for (int i = 0; i < nameChars.length; i++) {
                if (Character.isLetterOrDigit(nameChars[i])) {
                    nameChars[end++] = nameChars[i]; 
                }
            }
            if (end > 0) { 
                writer.write(nameChars, 0, end);
            }
            writer.write("'>");
            writer.write("<!-- --></a>");
        }
        if (params.getContent() != null) {
            writer.write(params.getContent());
        }
        writer.write("</span>");
    }
}



