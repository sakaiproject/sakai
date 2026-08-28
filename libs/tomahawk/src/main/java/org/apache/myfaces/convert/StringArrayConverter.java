/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.convert;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.StringTokenizer;

/**
 * @author Thomas Spiegl (latest modification by $Author: grantsmith $)
 * @version $Revision: 472630 $ $Date: 2006-11-08 15:40:03 -0500 (Wed, 08 Nov 2006) $
 */
public class StringArrayConverter
    implements Converter
{
    public Object getAsObject(FacesContext context, UIComponent component, String value)
        throws ConverterException
    {
        try
        {
            StringTokenizer tokenizer = new StringTokenizer(value, ",");
            String[] newValue = new String[tokenizer.countTokens()];
            for (int i = 0; tokenizer.hasMoreTokens(); i++)
            {
                newValue[i] = URLDecoder.decode(tokenizer.nextToken(), "UTF-8");
            }
            return newValue;
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    public String getAsString(FacesContext context, UIComponent component, Object value)
            throws ConverterException
    {
        return getAsString((String[])value,
                           true);   //escapeCommas
    }


    public static String getAsString(String[] strings,
                                     boolean escapeCommas)
    {
        try
        {
            if (strings == null || strings.length == 0)
            {
                return null;
            }
            else if (strings.length == 1)
            {

                return escapeCommas
                        ? URLEncoder.encode(strings[0], "UTF-8") //Encode, so that commas within Strings are escaped
                        : strings[0];
            }
            else
            {
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < strings.length; i++)
                {
                    if (i > 0)
                    {
                        buf.append(',');
                    }

                    String s = strings[i];

                    if(s!=null)
                    {
                        if (escapeCommas)
                        {
                            //Encode, so that commas within Strings are escaped
                            s = URLEncoder.encode(s, "UTF-8");
                        }
                        buf.append(s);
                    }
                }
                return buf.toString();
            }
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

}
