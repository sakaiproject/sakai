package uk.ac.cam.caret.sakai.rwiki.component.macros;

import java.io.IOException;
import java.io.Writer;

import org.radeox.api.macro.MacroParameter;
import org.radeox.macro.BaseMacro;

import uk.ac.cam.caret.sakai.rwiki.component.Messages;

/**
 * Basic PreMacro to render the contents as-is
 * contents. 
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public class PreMacro extends BaseMacro {
	
	public String getName(){
		return "pre";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.radeox.macro.Macro#getDescription()
	 */
	public String getDescription()
	{
		return Messages.getString("PreMacro.1");
	}

	public void execute(Writer writer, MacroParameter params) throws IllegalArgumentException, IOException {
		writer.write("<pre>");

		if (params.getContent() != null) {
			writer.write(params.getContent());
		}
		writer.write("</pre>");
	}


}
