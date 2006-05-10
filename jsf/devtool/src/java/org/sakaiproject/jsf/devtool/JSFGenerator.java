/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Sakai Foundation.
* 
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.jsf.devtool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

public class JSFGenerator
{	

	private static final String OUTPUT_DIR = "/w/jsf/widgets/src/java/org/sakaiproject/jsf";
	private static final String PIECES_DIR = "./pieces";
	private static final String TAG_DIR = "./tags";
	
	private static final Set standardAttributes = new HashSet(Arrays.asList(new String[] {"id", "binding", "rendered"}));
	
	public static void main(String[] args)
	throws Exception
	{
		
		JSFGenerator g = new JSFGenerator();

		// initialize Velocity
		Velocity.init("velocity.properties");
		
		// load attribute descriptions
		Properties attributeDescriptions = new Properties();
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(PIECES_DIR+"/attribute_descriptions.txt")));
		
		String line = null;
		String curTag = null;
		String curDesc = null;
		
		while ((line = in.readLine()) != null)
		{
			line = line.trim();
			if (line.startsWith("#")) continue;
			
			if (line.length() == 0)
			{
				if (curTag != null && curDesc != null)
				{
					attributeDescriptions.setProperty(curTag, curDesc);
				}
				curTag = null;
				curDesc = null;
			}
			else if (curTag == null)
			{
				curTag = line;
			}
			else
			{
				if (curDesc == null)
				{
					curDesc = line;
				}
				else
				{
					curDesc = curDesc + "\n" + line;
				}
			}
		}

		// find all tag definitions, and generate JSF tags for each one
		File dir = new File(TAG_DIR);
		File[] files = dir.listFiles();
		for (int i=0; i<files.length; i++)
		{
			File f = files[i];
			String filename = f.getName();
			if (filename.endsWith(".tag"))
			{
				g.generate(f, OUTPUT_DIR, PIECES_DIR, attributeDescriptions, standardAttributes);
				System.out.println("Generated tag: " + filename);
			}
		}
		
	}
	
	
	public void generate(File file, String outputBaseDir, String piecesDir, Properties attributeDescriptions, Set standardAttributes)
		throws Exception
	{
		String tag = file.getName().substring(0, file.getName().length()-4);
		
		
		String[] templates = {"Tag", "Component", "Renderer", "faces-config_component", "faces-config_renderer", "tld"};
		String[] outputpostfixes = {"Tag.java", "Component.java", "Renderer.java", "faces-config_component.xml", "faces-config_renderer.xml", "sakai_jsf.tld"}; 
		String[] outputprefixes = {"tag/", "component/", "renderer/", "", "", ""};
		boolean[] outputconcat = {false, false, false, true, true, true};
		
		Properties tagProps = new Properties();
		tagProps.load(new FileInputStream(file));
				
		// set props from file
        VelocityContext context = new VelocityContext();
		Enumeration e = tagProps.keys();
		while (e.hasMoreElements())
		{
			String key = (String) e.nextElement();
			context.put(key, tagProps.get(key));
		}
		
		// get rendered output from file
		BufferedReader in = new BufferedReader(new StringReader(tagProps.getProperty("sampleOutput")));
		String l;
		ArrayList lines = new ArrayList();
		while ((l = in.readLine()) != null)
		{
			lines.add(l.replaceAll("\\\"", "\\\\\""));
		}
		in.close();
		
		// set calculated props
		context.put("rendered", lines);
		context.put("caps", this);

		String attrsStr = tagProps.getProperty("attrs");
		String[] attrs = attrsStr.split(",");
		Set attrsSet = new HashSet(Arrays.asList(attrs));
		Set tagAttrsSet = new HashSet(attrsSet);
		tagAttrsSet.removeAll(standardAttributes);
		
		context.put("attrdescriptions", attributeDescriptions);
		context.put("tag", tag);
		context.put("attrs", attrs);
		context.put("tagattrs", tagAttrsSet);
		context.put("componentType", "org.sakaiproject."+caps(tag));
		context.put("rendererType", "org.sakaiproject."+caps(tag));
		context.put("tagClassName", caps(tag)+"Tag");
		context.put("componentClassName", caps(tag)+"Component");
		context.put("rendererClassName", caps(tag)+"Renderer");
		context.put("tagClass", "org.sakaiproject.jsf.tag."+caps(tag)+"Tag");
		context.put("componentClass", "org.sakaiproject.jsf.component."+caps(tag)+"Component");
		context.put("rendererClass", "org.sakaiproject.jsf.renderer."+caps(tag)+"Renderer");
		
		for (int i=0; i<templates.length; i++)
		{
            BufferedWriter out;
            
			String inputvmname = piecesDir+"/"+templates[i]+".vm";
			String outputfilename;
			if (!outputconcat[i])
			{
				outputfilename = outputBaseDir+"/"+outputprefixes[i]+caps(tag)+outputpostfixes[i];
			}
			else
			{
				outputfilename = outputBaseDir+"/"+outputprefixes[i]+outputpostfixes[i];
			}
			
			Template template = Velocity.getTemplate(inputvmname);

            out = new BufferedWriter(new FileWriter(outputfilename, outputconcat[i]));

            template.merge(context, out);

            out.flush();
            out.close();
            //System.out.println("Output "+outputfilename);
		}
	}
		
	/** Capitalize first letter */
	public String caps(String arg)
	{
		return arg.substring(0, 1).toUpperCase() + arg.substring(1, arg.length());
	}
	
}



