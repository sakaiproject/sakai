/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.component.app.help;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.sakaiproject.api.app.help.RestConfiguration;

/**
 * @author <a href="mailto:jlannan.iupui.edu">Jarrod Lannan</a>
 * @version $Id$
 * 
 */
@Slf4j
public class RestConfigurationImpl implements RestConfiguration
{

  /** user:pass as string ... will be converted to Base64 **/
  private String restCredentials;

  private String organization;
  private String restDomain;
  private String restUrl;
  private long cacheInterval;

  private static String REST_DOMAIN_URL;
  private static String REST_CORPUS_URL;

  /**
   * @see org.sakaiproject.api.app.help.RestConfiguration#getOrganization()
   */
  public String getOrganization()
  {
    return organization;
  }

  /**
   * @see org.sakaiproject.api.app.help.RestConfiguration#setOrganization(java.lang.String)
   */
  public void setOrganization(String organization)
  {
    this.organization = organization;
  }

  /**
   * @see org.sakaiproject.api.app.help.RestConfiguration#getRestCredentials()
   */
  public String getRestCredentials()
  {
    return restCredentials;
  }

  /**
   * @see org.sakaiproject.api.app.help.RestConfiguration#setRestCredentials(java.lang.String)
   */
  public void setRestCredentials(String restCredentials)
  {
    this.restCredentials = restCredentials;
  }

  /**
   * @see org.sakaiproject.api.app.help.RestConfiguration#getRestDomain()
   */
  public String getRestDomain()
  {
    return restDomain;
  }

  /**
   * @see org.sakaiproject.api.app.help.RestConfiguration#setRestDomain(java.lang.String)
   */
  public void setRestDomain(String restDomain)
  {
    this.restDomain = restDomain;
  }

  /**
   * @see org.sakaiproject.api.app.help.RestConfiguration#getRestUrl()
   */
  public String getRestUrl()
  {
    return restUrl;
  }

  /**
   * @see org.sakaiproject.api.app.help.RestConfiguration#setRestUrl(java.lang.String)
   */
  public void setRestUrl(String restUrl)
  {
    this.restUrl = restUrl;
  }

  /**
   * @see org.sakaiproject.api.app.help.RestConfiguration#getCacheInterval()
   */
  public long getCacheInterval()
  {
    return cacheInterval;
  }

  /**
   * @see org.sakaiproject.api.app.help.RestConfiguration#setCacheInterval(long)
   */
  public void setCacheInterval(long cacheInterval)
  {
    this.cacheInterval = cacheInterval;
  }

  /**
   * @see org.sakaiproject.api.app.help.RestConfiguration#getRestUrlInDomain()
   */
  public String getRestUrlInDomain()
  {
    if (REST_DOMAIN_URL != null)
    {
      return REST_DOMAIN_URL;
    }
    else
    {
      return REST_DOMAIN_URL = getRestUrl() + "/" + getRestDomain() + "/"
          + "document/" + getRestDomain() + "/";
    }
  }

  /**
   * @see org.sakaiproject.api.app.help.RestConfiguration#getRestCorpusUrl()
   */
  public String getRestCorpusUrl()
  {
    if (REST_DOMAIN_URL != null)
    {
      return REST_CORPUS_URL;
    }
    else
    {
      return REST_CORPUS_URL = getRestUrl() + "/" + getRestDomain() + "/"
          + "documents";
    }
  }

  /**
   * @see org.sakaiproject.api.app.help.RestConfiguration#getCorpusDocument()
   */
  public String getCorpusDocument()
  {

    if (log.isDebugEnabled())
    {
      log.debug("getCorpusDocument()");
    }

    URL url = null;
    StringBuilder sBuffer = new StringBuilder();
    BufferedReader br = null;
    try
    {
      url = new URL(getRestCorpusUrl());
      URLConnection urlConnection = url.openConnection();

      String basicAuthUserPass = getRestCredentials();
      String encoding = Base64.encodeBase64(basicAuthUserPass.getBytes("utf-8")).toString();
      
      urlConnection.setRequestProperty("Authorization", "Basic " + encoding);

      br = new BufferedReader(new InputStreamReader(urlConnection
          .getInputStream()), 512);
      int readReturn = 0;
      char[] cbuf = new char[512];
      while ((readReturn = br.read(cbuf, 0, 512)) != -1)
      {
        sBuffer.append(cbuf, 0, readReturn);
      }

    }
    catch (MalformedURLException e)
    {
      log.error("Malformed URL in REST document: " + url.getPath(), e);
    }
    catch (IOException e)
    {
      log.error("Could not open connection to REST document: " + url.getPath(),
          e);
    }
    finally
    {
      try
      {
        if (br != null)
        {
          br.close();
        }
      }
      catch (IOException e)
      {
        log.error("error closing corpus doc", e);
      }
    }

    return sBuffer.toString();
  }

  /**
   * @see org.sakaiproject.api.app.help.RestConfiguration#getResourceNameFromCorpusDoc(java.lang.String)
   */
  public String getResourceNameFromCorpusDoc(String xml)
  {
    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      DocumentBuilder builder = dbf.newDocumentBuilder();
      StringReader sReader = new StringReader(xml);
      InputSource inputSource = new org.xml.sax.InputSource(sReader);
      org.w3c.dom.Document xmlDocument = builder.parse(inputSource);
      sReader.close();
      
      NodeList nodeList = xmlDocument.getElementsByTagName("kbq");
      
      int nodeListLength = nodeList.getLength();
      for (int i = 0; i < nodeListLength; i++){
        Node currentNode = nodeList.item(i);
        
        NodeList nlChildren = currentNode.getChildNodes();
        
        for (int j = 0; j < nlChildren.getLength(); j++){
          if (nlChildren.item(j).getNodeType() == Node.TEXT_NODE){
            return nlChildren.item(j).getNodeValue();
          }
        }
      }
      return null;
    }            
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
    }     
    
    return null;
  }

}
