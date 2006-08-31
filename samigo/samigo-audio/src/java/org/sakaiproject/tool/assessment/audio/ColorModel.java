package org.sakaiproject.tool.assessment.audio;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.*;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.HashMap;
import java.awt.Color;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ColorModel
{
  private static final String RESOURCE_PACKAGE = "org.sakaiproject.tool.assessment.audio";
  private static final String RESOURCE_NAME = "colors";
  private static ResourceBundle colors = ResourceBundle.getBundle(RESOURCE_PACKAGE + "." +
    RESOURCE_NAME, Locale.getDefault());
  private static HashMap map = new HashMap();
  private static Log log = LogFactory.getLog(ColorModel.class);
  
  ColorModel()
  {
      if (map.size()==0)
      {
//        log.debug("Map size zero.");
        Enumeration cEnum = colors.getKeys();
        while (cEnum.hasMoreElements())
        {
          String colorName = (String) cEnum.nextElement();
          String colorString = (String) colors.getString(colorName);
//          log.debug(colorName + "=" + colorString);
          Color color = makeColor(colorString);
          map.put(colorName, color);
//          log.debug("DEBUG: " + getColor(colorName));
        }
      }
      else
      {
//        log.debug("map.size()="+map.size());
      }
  }

  public Color getColor(String colorName)
  {
    return (Color) map.get(colorName);
  }

  /**
   * Make a Color from a comma delimited RGB value
   * @param colorString comma delimited decimal or hex values
   *
   * @return a Color
   */
  private Color makeColor(String colorString)
  {
    int colorArray[] = { 0, 0, 0 };
    String colorNames[] = {"RED", "GREEN", "BLUE" };
    StringTokenizer st = new StringTokenizer(colorString, ",");
    for (int i=0; st.hasMoreElements(); i++)
    {
      try
      {
        String val = st.nextToken();
        if (val.startsWith("0x"))
        {
          colorArray[i] = Integer.parseInt(val.substring(2), 16);
        }
        else
        {
          colorArray[i] = Integer.parseInt(val);
        }
      }
      catch (NumberFormatException ex)
      {
        log.error("Unable to read color " + colorNames[i] + " value.");
      }
    }
    return new Color(colorArray[0],colorArray[1],colorArray[2]);
  }

  // test
  public static void main(String s[])
  {
    new ColorModel();
    new ColorModel();
  }

}