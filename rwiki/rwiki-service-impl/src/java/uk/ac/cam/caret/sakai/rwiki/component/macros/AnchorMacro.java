package uk.ac.cam.caret.sakai.rwiki.component.macros;

import java.io.IOException;
import java.io.Writer;

import org.radeox.macro.BaseMacro;
import org.radeox.macro.parameter.MacroParameter;

public class AnchorMacro extends BaseMacro {

    private static String[] paramDescription = {
            "1: An name to assign to this anchor."
        };

    private static String description = "Creates an anchor around a section of rwiki rendered content.";

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

    public void execute(Writer writer, MacroParameter params)
            throws IllegalArgumentException, IOException {
        
        writer.write("<a name='");
        
        char[] nameChars = params.get(0).toCharArray();
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
        if (params.getContent() != null) {
            writer.write(params.getContent());
        }
        writer.write("</a>");        
    }

    public String getName() {
        return "anchor";
    }

}
