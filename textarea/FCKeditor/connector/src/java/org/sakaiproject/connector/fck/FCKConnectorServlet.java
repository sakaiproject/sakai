/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
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

package org.sakaiproject.connector.fck;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Conenctor Servlet to upload and browse files to a Sakai worksite for the FCK editor.<br>
 *
 * This servlet accepts 4 commands used to retrieve and create files and folders from a worksite resource.
 * The allowed commands are:
 * <ul>
 * <li>GetFolders: Retrive the list of directory under the current folder
 * <li>GetFoldersAndFiles: Retrive the list of files and directory under the current folder
 * <li>CreateFolder: Create a new directory under the current folder
 * <li>FileUpload: Send a new file to the server (must be sent with a POST)
 * </ul>
 *
 * The current user must have a valid sakai session with permissions to access the realm associated 
 * with the resource.
 *
 * @author Joshua Ryan (joshua.ryan@asu.edu) merged servlets and Sakai-ified them
 * 
 * This connector is loosely based on two servlets found on the FCK website http://www.fckeditor.net/
 * written by Simone Chiaretta (simo@users.sourceforge.net)
 * 
 */

public class FCKConnectorServlet extends HttpServlet 
{
     
     /**
      * Manage the Get requests (GetFolders, GetFoldersAndFiles, CreateFolder).<br>
      *
      * The servlet accepts commands sent in the following format:<br>
      * connector?Command=CommandName&Type=ResourceType&CurrentFolder=FolderPath<br><br>
      * It executes the command and then return the results to the client in XML format.
      *
      * Valid values for Type are: Image, File, Flash and Link 
      *
      */
     public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
          
          response.setContentType("text/xml; charset=UTF-8");
          response.setHeader("Cache-Control","no-cache");
          PrintWriter out = response.getWriter();
          
          String commandStr = request.getParameter("Command");
          String type = request.getParameter("Type");
          String currentFolder = request.getParameter("CurrentFolder");

          Document document = null;
          try 
          {
               DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
               DocumentBuilder builder = factory.newDocumentBuilder();
               document = builder.newDocument();
          } 
          catch (ParserConfigurationException pce) 
          {
               pce.printStackTrace();
          }
          
          Node root = CreateCommonXml(document, commandStr, type, currentFolder, "/access/content"+currentFolder);
          
          if("GetFolders".equals(commandStr)) 
          {
               getFolders(currentFolder, root, document);
          }
          else if ("GetFoldersAndFiles".equals(commandStr)) 
          {
               getFolders(currentFolder, root, document);
               getFiles(currentFolder, root, document, type);
          }
          else if ("CreateFolder".equals(commandStr)) 
          {
               String newFolderStr = request.getParameter("NewFolderName");
               String status = "110";
               
               try 
               {
                    ResourcePropertiesEdit resourceProperties = ContentHostingService.newResourceProperties();
                    resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, newFolderStr);

                    ContentHostingService.addCollection(currentFolder + Validator.escapeResourceName(newFolderStr) + 
                             "/", resourceProperties);
                    status="0";
               }
               catch (IdUsedException iue) 
               {
                    status = "101";
               }
               catch(PermissionException sex) 
               {
                    System.out.println(sex);
                    status = "103";                    
               }               
               catch (Exception e) 
               {
                    status = "102";               
               }
               setCreateFolderResponse(status, root, document);
          }          
          
          document.getDocumentElement().normalize();
          try 
          {
               TransformerFactory tFactory = TransformerFactory.newInstance();
               Transformer transformer = tFactory.newTransformer();
               
               DOMSource source = new DOMSource(document);
     
               StreamResult result = new StreamResult(out);
               transformer.transform(source, result);

          } 
          catch (Exception ex) 
          {
               ex.printStackTrace();
          }
          
          out.flush();
          out.close();
     }
     

     /**
      * Manage the Post requests (FileUpload).<br>
      *
      * The servlet accepts commands sent in the following format:<br>
      * connector?Command=FileUpload&Type=ResourceType&CurrentFolder=FolderPath<br><br>
      * It stores the file (renaming it in case a file with the same name exists) and then return an HTML file
      * with a javascript command in it.
      *
      */     
     public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
     {

          response.setContentType("text/html; charset=UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          PrintWriter out = response.getWriter();

          String command = request.getParameter("Command");
          
          String type = request.getParameter("Type");
          String currentFolder = request.getParameter("CurrentFolder");
          
          String currentDirPath = "/access/content" + currentFolder;
          String fileName = "";
          String errorMessage = "";
          
          String status="0";

          if (!"FileUpload".equals(command) && !"QuickUpload".equals(command)) 
          {
               status = "203";
          }
          else 
          {
               DiskFileUpload upload = new DiskFileUpload();
               try 
               {
                    List items = upload.parseRequest(request);
               
                    Map fields = new HashMap();
               
                    Iterator iter = items.iterator();
                    while (iter.hasNext()) 
                    {
                        FileItem item = (FileItem) iter.next();
                        // System.out.println(item.getFieldName() + " === " + item);
                        if (item.isFormField()) 
                             fields.put(item.getFieldName(), item.getString());
                        else
                             fields.put(item.getFieldName(), item);
                    }
                    FileItem uplFile = (FileItem)fields.get("NewFile");

                    String filePath = uplFile.getName();
                    filePath = filePath.replace('\\','/');
                    String[] pathParts = filePath.split("/");
                    fileName = pathParts[pathParts.length-1];
                    
                    String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf(".")); 
                    String ext = fileName.substring(fileName.lastIndexOf(".") + 1); 

                    String mime = uplFile.getContentType();

                    int counter = 1;
                    boolean done = false;

                    while(!done)
                    {
                         try 
                         {
                             ResourcePropertiesEdit resourceProperties = ContentHostingService.newResourceProperties();
                             resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, fileName);
                          
                             int noti = NotificationService.NOTI_NONE;

                             ContentHostingService.addResource(currentFolder+fileName, mime, uplFile.get(), 
                                    resourceProperties, noti);
                             done = true;
                         }
                         catch (IdUsedException iue) 
                         {
                              //the name is already used, so we do a slight rename to prevent the colision
                              fileName = nameWithoutExt + "(" + counter + ")" + "." + ext;
                              status = "201";
                              counter++;
                         }

                         catch (Exception ex)  
                         {
                              //this user can't write where they are trying to write.
                              done = true;
                              ex.printStackTrace();
                              status = "203";
                         }
                    }
               }
               catch (Exception ex)  
               {
                    ex.printStackTrace();
                    status = "203";
               }
          }

          out.println("<script type=\"text/javascript\">");
          
          if ("QuickUpload".equals(command))  
          {
               out.println("window.parent.OnUploadCompleted("+status+",'"+currentDirPath+fileName+"','"+fileName+"','"+errorMessage+"');");
          }
          else 
          {
               out.println("window.parent.frames['frmUpload'].OnUploadCompleted("+status+",'"+fileName+"');");
          }
          
          out.println("</script>");
          
          out.flush();
          out.close();
          
     }

     private void setCreateFolderResponse(String status, Node root, Document doc) 
     {
          Element element = doc.createElement("Error");
          element.setAttribute("number", status);
          root.appendChild(element);
     }
     

     private void getFolders(String dir, Node root, Document doc) 
     {
          Element folders = doc.createElement("Folders");
          root.appendChild(folders);
                    
          ContentCollection collection = null;
         
          //prevent listings of root level nodes, which could have 1000's of items 
          if (dir.split("/").length < 3)
              return;       
   
          try 
          {
               collection = ContentHostingService.getCollection(dir);
          }
          catch (Exception e) 
          {
               //not a valid collection? file list will be empty and so will the doc
          }
          if (collection != null) 
          {
               String current = null;
               Iterator iterator = collection.getMembers().iterator();

               while (iterator.hasNext ()) 
               {
                    try 
                    {
                         current = (String)iterator.next();
                         ContentCollection myCollection = ContentHostingService.getCollection(current);
                         Element element=doc.createElement("Folder");
                         element.setAttribute("name", current.substring( ( 
                                current.substring(0, current.length()-1) ).lastIndexOf("/")+1, current.length()-1) );
                         folders.appendChild(element);
                    }
                    catch (Exception e) 
                    {
                         //do nothing, we either don't have access to the collction or it's a resource
                    }
               }          
          }
     }

     private void getFiles(String dir, Node root, Document doc, String type) 
     {
          Element files=doc.createElement("Files");
          root.appendChild(files);
          
          ContentCollection collection = null;
          
          try 
          {
               collection = ContentHostingService.getCollection(dir);
          }
          catch (Exception e) 
          {
               //do nothing, file will be empty and so will doc
          }     
          if (collection != null) 
          {
        	   Iterator iterator = collection.getMemberResources().iterator();
          
               while (iterator.hasNext ()) 
               {
                    try 
                    {
                    	ContentResource current = (ContentResource)iterator.next();

                         String ext = current.getProperties().getProperty(
                                   current.getProperties().getNamePropContentType());
                         
                         if ( (type.equals("File") && (ext != null) ) || 
                              (type.equals("Flash") && ext.equalsIgnoreCase("application/x-shockwave-flash") ) ||
                              (type.equals("Image") && ext.startsWith("image") ) ||
                              type.equals("Link")) 
                         {
                         
                              Element element=doc.createElement("File");
                              element.setAttribute("name", current.getProperties().getProperty(
                                      current.getProperties().getNamePropDisplayName()));
                              
                              if (current.getProperties().getProperty(
                                            current.getProperties().getNamePropContentLength()) != null)
                              {
                                   element.setAttribute("size", "" + current.getProperties().getProperty(
                                            current.getProperties().getNamePropContentLength()));
                              }
                              else 
                              {
                                   element.setAttribute("size", "0");
                                   
                              }
                              files.appendChild(element);
                         }
                    }
                    catch (ClassCastException e) 
                    {
                    	//it's a colleciton not an item
                    }
                    catch (Exception e) 
                    {
                    	//do nothing, we don't have access to the item
                    }
               }     
          }
     }     

     private Node CreateCommonXml(Document doc,String commandStr, String type, String currentPath, String currentUrl )
     {
          Element root = doc.createElement("Connector");
          doc.appendChild(root);
          root.setAttribute("command", commandStr);
          root.setAttribute("resourceType", type);
          
          Element element = doc.createElement("CurrentFolder");
          element.setAttribute("path", currentPath);
          element.setAttribute("url", currentUrl);
          root.appendChild(element);
          
          return root;
          
     }

}
