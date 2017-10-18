/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.connector.fck;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

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
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.codec.binary.Base64;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.FormattedText;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.sakaiproject.api.app.messageforums.entity.DecoratedForumInfo;
import org.sakaiproject.api.app.messageforums.entity.DecoratedTopicInfo;



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

public class FCKConnectorServlet extends HttpServlet {

	 private static Logger M_log = LoggerFactory.getLogger(FCKConnectorServlet.class);

     private static final long serialVersionUID = 1L;

     private static final String MFORUM_FORUM_PREFIX = "/direct/forum/";
     private static final String MFORUM_TOPIC_PREFIX = "/direct/forum_topic/";
     private static final String MFORUM_MESSAGE_PREFIX = "/direct/forum_message/";
     
     private static final String FCK_ADVISOR_BASE = "fck.security.advisor.";
     private static final String CK_ADVISOR_BASE = "ck.security.advisor.";
     private static final String FCK_EXTRA_COLLECTIONS_BASE = "fck.extra.collections.";

//   private String[] hiddenProviders = {"forum_message", "forum_topic"};
     //Default hidden providers
     private List <String> hiddenProviders;
     
     private String serverUrlPrefix = "";

     private ContentHostingService contentHostingService = null;
     private SecurityService securityService = null;
     private SessionManager sessionManager = null;
     private NotificationService notificationService = null;
     private SiteService siteService = null;
     private SecurityAdvisor contentNewAdvisor = null;
     private EntityBroker entityBroker;
     private ServerConfigurationService serverConfigurationService = null;
     private ResourceLoader resourceLoader = null;

     /**
      * Injects dependencies using the ComponentManager cover.
      * 
      * @param beanName The name of the bean to get.
      * @return the bean
      */
     private Object inject(String beanName) {
          Object bean = null;
          bean = ComponentManager.get(beanName);
          if (bean == null) {
               throw new IllegalStateException("FAILURE to inject dependency during initialization of the FCKConnectorServlet");
          }
          return bean;
     }

     /**
      * Ensures all necessary dependencies are loaded.
      */
     private void initialize() {
          if (contentHostingService == null) {
               contentHostingService = (ContentHostingService) inject("org.sakaiproject.content.api.ContentHostingService");
          }
          if (securityService == null) {
               securityService = (SecurityService) inject("org.sakaiproject.authz.api.SecurityService");
          }
          if (sessionManager == null) {
               sessionManager = (SessionManager) inject("org.sakaiproject.tool.api.SessionManager");
          }
          if (notificationService == null) {
               notificationService = (NotificationService) inject("org.sakaiproject.event.api.NotificationService");
          }
          if (siteService == null) {
              siteService = (SiteService) inject("org.sakaiproject.site.api.SiteService");
          if (entityBroker == null) {
        	   entityBroker = (EntityBroker) inject("org.sakaiproject.entitybroker.EntityBroker");
          }
          if (serverConfigurationService == null) {
        	  serverConfigurationService = (ServerConfigurationService) inject("org.sakaiproject.component.api.ServerConfigurationService");
          }

          //Default providers to exclude, add addition with the property textarea.hiddenProviders if needed 
//          hiddenProviders = Arrays.asList("");
          hiddenProviders = Arrays.asList("assignment","sam_pub","forum","forum_topic","topic");
     }

          if (resourceLoader == null) {
        	  resourceLoader = new ResourceLoader("org.sakaiproject.connector.fck.Messages");
          }
          if (contentNewAdvisor == null) {
        	  contentNewAdvisor = createSubmissionSecurityAdvisor();
          }
     }

     public void init() throws ServletException {
          super.init();
          initialize();
     }
     
     /**
     * pops a special private advisor when necessary
     * @param thisDir Directory where this is referencing
     * @param collectionBase base of the collection
     */
    private void popPrivateAdvisor(String thisDir, String collectionBase) {
    	 if (thisDir.startsWith("/private/")) {
    		 SecurityAdvisor advisor = (SecurityAdvisor) sessionManager.getCurrentSession().getAttribute(FCK_ADVISOR_BASE + collectionBase);
    		 if (advisor != null) {
    			 securityService.popAdvisor(advisor);
    		 }
    		 advisor = (SecurityAdvisor) sessionManager.getCurrentSession().getAttribute(CK_ADVISOR_BASE + collectionBase);
    		 if (advisor != null) {
    			 securityService.popAdvisor(advisor);
    		 }
    	 }
     }

    /**
     * pushes a special private advisor when necessary
     * @param thisDir Directory where this is referencing
     * @param collectionBase base of the collection
     */
     private void pushPrivateAdvisor(String thisDir, String collectionBase) {
    	 if (thisDir.startsWith("/private/")) {
    		 SecurityAdvisor advisor = (SecurityAdvisor) sessionManager.getCurrentSession().getAttribute(FCK_ADVISOR_BASE + collectionBase);
    		 if (advisor != null) {
    			 securityService.pushAdvisor(advisor);
    		 }
    		 advisor = (SecurityAdvisor) sessionManager.getCurrentSession().getAttribute(CK_ADVISOR_BASE + collectionBase);
    		 if (advisor != null) {
    			 securityService.pushAdvisor(advisor);
    		 }
    	 }
     }
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
          initialize();
          response.setContentType("text/xml; charset=UTF-8");
          response.setHeader("Cache-Control","no-cache");
          PrintWriter out = null;
          
          String commandStr = request.getParameter("Command");
          String type = request.getParameter("Type");
          String currentFolder = request.getParameter("CurrentFolder");

          serverUrlPrefix = serverConfigurationService.getServerUrl();

          String collectionBase = request.getPathInfo();

          Document document = null;
          try {
               DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
               DocumentBuilder builder = factory.newDocumentBuilder();
               document = builder.newDocument();
          } 
          catch (ParserConfigurationException pce) {
               pce.printStackTrace();
          }
          
          Node root = createCommonXml(document, commandStr, type, currentFolder, 
               contentHostingService.getUrl(currentFolder));
          
          //To support meletedocs, the call to getFolders has to be able to retrieve all folders that are part of melete private (which will be passed in) as 
          //well as ones that are not. This is why the advisor is being manipulated inside the getfolders method.
          
          if ("GetFolders".equals(commandStr)) {
               getFolders(currentFolder, root, document, collectionBase);
          }
          else if ("GetFoldersAndFiles".equals(commandStr)) {
               getFolders(currentFolder, root, document, collectionBase);
               //Might need this advisor for files
               pushPrivateAdvisor(currentFolder,collectionBase);
               getFiles(currentFolder, root, document, type);
               popPrivateAdvisor(currentFolder,collectionBase);
          }
          else if ("GetFoldersFilesAssignsTestsTopics".equals(commandStr)) {
             ConnectorHelper thisConnectorHelper = new ConnectorHelper();
             thisConnectorHelper.init();
             getFolders(currentFolder, root, document, collectionBase);
             //Might need this advisor for files
             pushPrivateAdvisor(currentFolder,collectionBase);
             getFilesOnly(currentFolder, root, document, type);
             popPrivateAdvisor(currentFolder,collectionBase);
             getAssignmentsOnly(currentFolder, root, document, type, thisConnectorHelper);
             getTestsOnly(currentFolder, root, document, type, thisConnectorHelper);
             getOtherEntitiesOnly(currentFolder, root, document, type, thisConnectorHelper);
             
             getForumsAndThreads(currentFolder, root, document, type, thisConnectorHelper);
          }
          else if ("GetResourcesAssignsTestsTopics".equals(commandStr)) {
             ConnectorHelper thisConnectorHelper = new ConnectorHelper();
             thisConnectorHelper.init();
             pushPrivateAdvisor(currentFolder,collectionBase);
             getResources(currentFolder, root, document, collectionBase, type);
             popPrivateAdvisor(currentFolder,collectionBase);
             getAssignmentsOnly(currentFolder, root, document, type, thisConnectorHelper);
             getTestsOnly(currentFolder, root, document, type, thisConnectorHelper);
             getOtherEntitiesOnly(currentFolder, root, document, type, thisConnectorHelper);
             
             getForumsAndThreads(currentFolder, root, document, type, thisConnectorHelper);
          }
          else if ("GetResources".equals(commandStr)) {
        	  pushPrivateAdvisor(currentFolder,collectionBase);
              getResources(currentFolder, root, document, collectionBase, type);
        	  popPrivateAdvisor(currentFolder,collectionBase);
          }          
          
          
          else if ("CreateFolder".equals(commandStr)) {
               String newFolderStr = request.getParameter("NewFolderName");
               String status = "110";
               pushPrivateAdvisor(currentFolder,collectionBase);
               
               try {
                    ContentCollectionEdit edit = contentHostingService
                         .addCollection(currentFolder + Validator.escapeResourceName(newFolderStr) + Entity.SEPARATOR);
                    ResourcePropertiesEdit resourceProperties = edit.getPropertiesEdit();
                    resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, newFolderStr);
                    String altRoot = getAltReferenceRoot(currentFolder);
                    if (altRoot != null)
                         resourceProperties.addProperty (ContentHostingService.PROP_ALTERNATE_REFERENCE, altRoot);

                    contentHostingService.commitCollection(edit);
                    
                    status="0";
               }
               catch (IdUsedException iue) {
                    status = "101";
               }
               catch(PermissionException pex) {
                    status = "103";                    
               }               
               catch (Exception e) {
                    status = "102";               
               }
               setCreateFolderResponse(status, root, document);
               popPrivateAdvisor(currentFolder,collectionBase);
          }          
          
          document.getDocumentElement().normalize();
          try {
               out = response.getWriter();
               TransformerFactory tFactory = TransformerFactory.newInstance();
               Transformer transformer = tFactory.newTransformer();
               
               DOMSource source = new DOMSource(document);
     
               StreamResult result = new StreamResult(out);
               transformer.transform(source, result);

          } 
          catch (Exception ex) {
               ex.printStackTrace();
          }
          finally { 
               if (out != null) {
                    out.close();
               }
          }
          
		  //Pop the advisor if we need to
		  popPrivateAdvisor(currentFolder,collectionBase);
     }
     

    private static final int MAX_SAVE_RETRIES = 1000;

     /**
      * Manage the Post requests (FileUpload).<br>
      *
      * The servlet accepts commands sent in the following format:<br>
      * connector?Command=FileUpload&Type=ResourceType&CurrentFolder=FolderPath<br><br>
      * It stores the file (renaming it in case a file with the same name exists) and then return an HTML file
      * with a javascript command in it.
      *
      */     
     public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
          initialize();
          response.setContentType("text/html; charset=UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          PrintWriter out = null;

          String command = request.getParameter("Command");
          
          String currentFolder = request.getParameter("CurrentFolder");
          String collectionBase = request.getPathInfo();
          
          pushPrivateAdvisor(currentFolder,collectionBase);
          
          String fileName = "";
          String errorMessage = "";
          
          String status="0";
          ContentResource attachment = null;

          if (!"FileUpload".equals(command) && !"QuickUpload".equals(command) && !("QuickUploadEquation").equals(command) && !("QuickUploadAttachment").equals(command)) {
               status = "203";
          }
          else {
               DiskFileUpload upload = new DiskFileUpload();
               String mime="";
               InputStream requestStream = null;
               byte [] bytes=null;
               try {
                   //Special case for uploading fmath equations
                   if (("QuickUploadEquation").equals(command)) {
                       String image = request.getParameter("image");
                       String type = request.getParameter("type");
                       // size protection 
                       if(image==null || image.length()>1000000) return;
                       bytes = Base64.decodeBase64(image);
                       fileName = "fmath-equation-"+request.getParameter("name");
                       if ("PNG".equals(type)) {
                           mime = "image/png";
                           if (fileName.indexOf(".") == -1) {
                               fileName+=".png";
                           }
                       }
                       else { 
                           mime = "image/jpeg";
                           if (fileName.indexOf(".") == -1) {
                               fileName+=".jpg";
                           }
                       }
                   }
                   else {
                	   //If this is a multipart request
                	   if (ServletFileUpload.isMultipartContent(request)) {
	                       List items = upload.parseRequest(request);
	
	                       Map fields = new HashMap();
	
	                       Iterator iter = items.iterator();
	                       while (iter.hasNext()) {
	                           FileItem item = (FileItem) iter.next();
	                           if (item.isFormField()) {
	                               fields.put(item.getFieldName(), item.getString());
	                           }
	                           else {
	                               fields.put(item.getFieldName(), item);
	                           }
	                       }
	                       FileItem uplFile = (FileItem)fields.get("NewFile");
	
	                       String filePath = uplFile.getName();
	                       filePath = filePath.replace('\\','/');
	                       String[] pathParts = filePath.split("/");
	                       fileName = pathParts[pathParts.length-1];
	                       mime = uplFile.getContentType();
	                       requestStream = uplFile.getInputStream();
	                   }
                	   else {
                		   requestStream = request.getInputStream();
                		   mime = request.getHeader("Content-Type");
                           }
                	   }
                		   
                    //If there's no filename, make a guid name with the mime extension?
                    if ("".equals (fileName)) {
                    	fileName = UUID.randomUUID().toString();
                    }
                    String nameWithoutExt = fileName; 
                    String ext = ""; 

                    if (fileName.lastIndexOf(".") > 0) {
                         nameWithoutExt = fileName.substring(0, fileName.lastIndexOf(".")); 
                         ext = fileName.substring(fileName.lastIndexOf(".")); 
                    }


                    int counter = 1;
                    boolean done = false;
                    Throwable lastException = null;

                    for (int retry = 0; !done && retry < MAX_SAVE_RETRIES; retry++) {
                         try {
                             ResourcePropertiesEdit resourceProperties = contentHostingService.newResourceProperties();
                             resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, fileName);

                             if ("QuickUploadEquation".equals(command) || "QuickUploadAttachment".equals(command)) {
                            	 attachment = bypassAddAttachment(request,fileName,mime,bytes,requestStream,resourceProperties);
                             } else {
	                             String altRoot = getAltReferenceRoot(currentFolder);
	                             if (altRoot != null)
	                                  resourceProperties.addProperty (ContentHostingService.PROP_ALTERNATE_REFERENCE, altRoot);
	
	                             int noti = NotificationService.NOTI_NONE;
	                             if (bytes != null) {
	                            	 contentHostingService.addResource(currentFolder+fileName, mime, bytes, resourceProperties, noti);
	                             }
	                             else if (requestStream != null) {
	                            	 contentHostingService.addResource(currentFolder+fileName, mime, requestStream, resourceProperties, noti);
	                             }
	                             else {
	                            	 //Nothing to do
	                            	 
	                             }
                             }

                             done = true;
                         }
                         catch (IdUsedException iue) {
                              //the name is already used, so we do a slight rename to prevent the colision
                              lastException = iue;
                              fileName = nameWithoutExt + "(" + counter + ")" + ext;
                              status = "201";
                              counter++;
                         }

                         catch (Exception ex) {
                              //this user can't write where they are trying to write.
                              done = true;
                              lastException = ex;
                              ex.printStackTrace();
                              status = "203";
                         }
                    }

                    if (!done) {
                        // Hit our limit on retries.  This shouldn't happen
                        // unless the state of things is strange (see SAK-32346
                        // for an example of that)
                        throw new RuntimeException("Retry limit exceeded", lastException);
                    }

               }
               catch (Exception ex) {
                    ex.printStackTrace();
                    status = "203";
               }
          }

          try {
               out = response.getWriter();
               if ("QuickUploadEquation".equals(command) || "QuickUploadAttachment".equals(command)) {
                   out.println(attachment!=null?attachment.getUrl():"");
               }
               else {
                   out.println("<script type=\"text/javascript\">");
               	   out.println("(function(){ var d = document.domain ; while ( true ) {");
                   out.println("try { var test = parent.document.domain ; break ; } catch( e ) {}");
                   out.println("d = d.replace( /.*?(?:\\.|$)/, '' ) ; if ( d.length == 0 ) break ;");
                   out.println("try { document.domain = d ; } catch (e) { break ; }}})() ;");

                       out.println("window.parent.OnUploadCompleted(" + status + ",'"
                               + (attachment!=null?attachment.getUrl():"")
                               + "','" + FormattedText.escapeJsQuoted(fileName) + "','" + errorMessage + "');");

                   out.println("</script>");
               }
          }
          catch (Exception e) {
               e.printStackTrace();
          }
          finally {         
               if (out != null) {
                    out.close();
               }
          }
          
          popPrivateAdvisor(currentFolder,collectionBase);
     }

     private void setCreateFolderResponse(String status, Node root, Document doc) {
          Element element = doc.createElement("Error");
          element.setAttribute("number", status);
          root.appendChild(element);
     }
     

     private void getFolders(String dir, Node root, Document doc, String collectionBase) {
          Element folders = doc.createElement("Folders");
          root.appendChild(folders);
                    
          ContentCollection collection = null;
         
          Map<String, String> map = null; 
          List <Iterator> foldersIterator = new ArrayList <Iterator> ();
   
          try {
               //hides the real root level stuff and just shows the users the
               //the root folders of all the top collections they actually have access to.
        	   int dirlength = dir.split("/").length;
               if (dirlength == 2 || dir.startsWith("/private/")) {
                    List<String> collections = new ArrayList<String>();
                    map = contentHostingService.getCollectionMap();
                    for (String key : map.keySet()) {
                    	if (!contentHostingService.isInDropbox((String)key)) {
                    		collections.add(key);
                    	}

                    }
                    List extras = (List) sessionManager.getCurrentSession()
                         .getAttribute(FCK_EXTRA_COLLECTIONS_BASE + collectionBase);
                    if (extras != null) {
                         collections.addAll(extras);
                    }

                    foldersIterator.add(collections.iterator());
               }
               if (dirlength > 2) {
            	   pushPrivateAdvisor(dir,collectionBase);
            	   collection = contentHostingService.getCollection(dir);
            	   if (collection != null && collection.getMembers() != null) {
            		   foldersIterator.add(collection.getMembers().iterator());
            	   }
            	   popPrivateAdvisor(dir,collectionBase);
               }          
          }
          catch (Exception e) {    
               e.printStackTrace();
               //not a valid collection? file list will be empty and so will the doc
          }
          for (Iterator folderIterator : foldersIterator ) {
        	  if (folderIterator != null) {
        		  String current = null;

        		  // create a SortedSet using the elements that are going to be added to the XML doc
        		  SortedSet<Element> sortedFolders = new TreeSet<Element>(new SortElementsForDisplay());

        		  while (folderIterator.hasNext()) {
        			  try {
        				  current = (String) folderIterator.next();
       					  pushPrivateAdvisor(current,collectionBase);
        				  ContentCollection myCollection = contentHostingService.getCollection(current);
        				  Element element=doc.createElement("Folder");
        				  element.setAttribute("url", current);
        				  String collectionName =  myCollection.getProperties().getProperty(myCollection.getProperties().getNamePropDisplayName());
        				  if (current.contains("/meleteDocs/")) {
        					  if (resourceLoader != null)
        						  collectionName =  resourceLoader.getString("melete_collectionname");
        					  else
        						  collectionName = "Melete Files";
        				  }
        				  element.setAttribute("name",collectionName); 
        				  // by adding the folders to this collection, they will be sorted for display
        				  sortedFolders.add(element);
       					  popPrivateAdvisor(current,collectionBase);
        			  }
        			  catch (Exception e) {    
        				  //do nothing, we either don't have access to the collction or it's a resource
        				  M_log.debug("No access to display collection" + e.getMessage());
        			  }
        		  }      

        		  // now append the folderse to the parent document in sorted order
        		  for (Element folder: sortedFolders) {
        			  folders.appendChild(folder);
        		  }
        	  }
          }
     }

     private void getResources(String dir, Node root, Document doc, String collectionBase, String type) {
         Element folders = doc.createElement("Folders");
         root.appendChild(folders);
                   
         ContentCollection collection = null;
        
         Map<String, String> map = null; 
         Iterator foldersIterator = null;
  
         try {
              //hides the real root level stuff and just shows the users the
              //the root folders of all the top collections they actually have access to.
              if (dir.split("/").length == 2) {
                   List<String> collections = new ArrayList<String>();
                   map = contentHostingService.getCollectionMap();
                   if (map != null && map.keySet() != null) {
                        collections.addAll(map.keySet());
                   }
                   List extras = (List) sessionManager.getCurrentSession()
                        .getAttribute(FCK_EXTRA_COLLECTIONS_BASE + collectionBase);
                   if (extras != null) {
                        collections.addAll(extras);
                   }

                   foldersIterator = collections.iterator();
              }
              else if (dir.split("/").length > 2) {
                   collection = contentHostingService.getCollection(dir);
                   if (collection != null && collection.getMembers() != null) {
                        foldersIterator = collection.getMembers().iterator();
                   }
              }          
         }
         catch (Exception e) {    
              e.printStackTrace();
              //not a valid collection? file list will be empty and so will the doc
         }
         if (foldersIterator != null) {
              String current = null;
              
              // create a SortedSet using the elements that are going to be added to the XML doc
              SortedSet<Element> sortedFolders = new TreeSet<Element>(new SortElementsForDisplay());
              
              while (foldersIterator.hasNext()) {
                   try {
                        current = (String) foldersIterator.next();
                        ContentCollection myCollection = contentHostingService.getCollection(current);
                        Element element=doc.createElement("Folder");
                        element.setAttribute("path", current);
			 element.setAttribute("url", contentHostingService.getUrl(current));
                        element.setAttribute("name", myCollection.getProperties().getProperty(
                                             myCollection.getProperties().getNamePropDisplayName()));
                        // SAK-27756 Added children count to decide whether to show expand button as we removed nested iteration of files
                        element.setAttribute("childrenCount", myCollection.getMemberCount()+"");
                        // by adding the folders to this collection, they will be sorted for display
                        sortedFolders.add(element);
                   }
                   catch (Exception e) {    
                        //do nothing, we either don't have access to the collction or it's a resource
                   }
              }      
              
              // now append the folderse to the parent document in sorted order
              for (Element folder: sortedFolders) {
                 Node addedFolder = folders.appendChild(folder);
              }

              getFilesOnly(dir, root, doc, type);
         }
    }
     
     
     
     private void getFiles(String dir, Node root, Document doc, String type) {
          Element files=doc.createElement("Files");
          root.appendChild(files);
          
          ContentCollection collection = null;
          
          try {
               collection = contentHostingService.getCollection(dir);
          }
          catch (Exception e) {
               //do nothing, file will be empty and so will doc
          }     
          if (collection != null) {
               Iterator iterator = collection.getMemberResources().iterator();
               
               // create a SortedSet using the elements that are going to be added to the XML doc
               SortedSet<Element> sortedFiles = new TreeSet<Element>(new SortElementsForDisplay());               
          
               while (iterator.hasNext ()) {
                    try {
                         ContentResource current = (ContentResource)iterator.next();

                         String ext = current.getProperties().getProperty(
                                   current.getProperties().getNamePropContentType());
                         
                         if ( ("File".equals(type) && (ext != null) ) || 
                              ("Flash".equals(type) && ext.equalsIgnoreCase("application/x-shockwave-flash") ) ||
                              ("Image".equals(type) && ext.startsWith("image") ) ||
                              ("Media".equals(type) && (ext.startsWith("video") || ext.startsWith("audio") || ext.equalsIgnoreCase("application/x-shockwave-flash")) ) ||
                              "Link".equals(type)) {

                              String id = current.getId();
                             
                              Element element=doc.createElement("File");
                              // displaying the id instead of the display name because the url used
                              // for linking in the FCK editor uses what is returned...
                              element.setAttribute("name", current.getProperties().getProperty(
                                                   current.getProperties().getNamePropDisplayName()));
                              element.setAttribute("url", current.getUrl());
                              
                              if (current.getProperties().getProperty(
                                            current.getProperties().getNamePropContentLength()) != null) {

                                   element.setAttribute("size", "" + current.getProperties()
                                        .getPropertyFormatted(current.getProperties()
                                        .getNamePropContentLength()));
                              }
                              else {
                                   element.setAttribute("size", "0");
                                   
                              }
                              // by adding the files to this collection, they will be sorted for display
                              sortedFiles.add(element);
                         }
                    }
                    catch (ClassCastException e)  {
                         //it's a colleciton not an item
                    }
                    catch (Exception e)  {
                         //do nothing, we don't have access to the item
                    }
               }
               
               // now append the files to the parent document in sorted order
               for (Element file: sortedFiles) {
                  files.appendChild(file);
               }
          }
     }     

     private void getFilesOnly(String dir, Node root, Document doc, String type) {
         Element files=doc.createElement("Files");
         root.appendChild(files);
         
         ContentCollection collection = null;
         List myAssignments = null;
         List myTests = null;
         List myTopics = null;
         String siteId = null;
   	  String[] f = dir.split("/");
   	  siteId = f[2];

         
         
         try {
              collection = contentHostingService.getCollection(dir);
         }
         catch (Exception e) {
              //do nothing, file will be empty and so will doc
         }     
         if (collection != null) {
       	   Iterator iterator = collection.getMemberResources().iterator();
         
              while (iterator.hasNext ()) {
                   try {
                   	 ContentResource current = (ContentResource)iterator.next();

                        String ext = current.getProperties().getProperty(
                                  current.getProperties().getNamePropContentType());
                        
                        if ( ("File".equals(type) && (ext != null) ) || 
                             ("Flash".equals(type) && ext.equalsIgnoreCase("application/x-shockwave-flash") ) ||
                             ("Image".equals(type) && ext.startsWith("image") ) ||
                             "Link".equals(type)) {

                       	 String id = current.getId();
                            
                             Element element=doc.createElement("File");
                             // displaying the id instead of the display name because the url used
                             // for linking in the FCK editor uses what is returned...
                             element.setAttribute("name", current.getProperties().getProperty(
                           		  current.getProperties().getNamePropDisplayName()));
                             element.setAttribute("url", current.getUrl());
                             
                             if (current.getProperties().getProperty(
                                           current.getProperties().getNamePropContentLength()) != null) {

                                  element.setAttribute("size", "" + current.getProperties()
                                       .getPropertyFormatted(current.getProperties()
                                       .getNamePropContentLength()));
                             }
                             else {
                                  element.setAttribute("size", "0");
                                  
                             }
                             files.appendChild(element);
                        }
                   }
                   catch (ClassCastException e)  {
                   	//it's a colleciton not an item
                   }
                   catch (Exception e)  {
                   	//do nothing, we don't have access to the item
                   }
              }     
         }
        
    }
    private void getAssignmentsOnly(String dir, Node root, Document doc, String type, ConnectorHelper ch) {
    	
        List myAssignments = null;
        String siteId = null;
     	String[] f = dir.split("/");
       	siteId = f[2];

	SortedSet<Element> sortedItems = new TreeSet<Element>(new SortElementsForDisplay());
        
        Element assignments=doc.createElement("Assignments");
        root.appendChild(assignments);
        myAssignments = ch.getSiteAssignments(siteId);
        Iterator assignmentsIterator = myAssignments.iterator();
        while(assignmentsIterator.hasNext()){
     	   	String[] thisAssignmentReference = (String[]) assignmentsIterator.next();
     	   	Element element=doc.createElement("Assignment");
     	   	element.setAttribute("name",thisAssignmentReference[0]);
     	   	element.setAttribute("url",serverUrlPrefix + thisAssignmentReference[1]);
     	   	element.setAttribute("size", "0");
     	   	sortedItems.add(element);
        }

	for (Element item: sortedItems) {
		assignments.appendChild(item);
	}
          	
    }
    
    private void getTestsOnly(String dir, Node root, Document doc, String type, ConnectorHelper ch) {
    	
        List myTests = null;
        String siteId = null;
     	String[] f = dir.split("/");
       	siteId = f[2];

	SortedSet<Element> sortedItems = new TreeSet<Element>(new SortElementsForDisplay());

        Element assessments=doc.createElement("Assessments");
        root.appendChild(assessments);
        myTests = ch.getPublishedAssements(siteId);
        Iterator assessmentIterator = myTests.iterator();
        while(assessmentIterator.hasNext()){
      	  String[] thisAssessmentReference = (String[]) assessmentIterator.next();
       	   	Element element=doc.createElement("Assessment");
         	   	element.setAttribute("name",thisAssessmentReference[0]);
         	   	element.setAttribute("url",serverUrlPrefix + thisAssessmentReference[1]);
         	   	element.setAttribute("size", "0");
         	   	sortedItems.add(element);           	  
        }

	for (Element item: sortedItems) {
		assessments.appendChild(item);
	}

    }
    
    private void getOtherEntitiesOnly(String dir, Node root, Document doc, String type, ConnectorHelper ch) {
    	
        List myTests = null;
        String siteId = null;
        String user = sessionManager.getCurrentSessionUserId();
     	String[] f = dir.split("/");
       	siteId = f[2];

       	SortedSet<Element> sortedItems = new TreeSet<Element>(new SortElementsForDisplay());

        Element otherEntities=doc.createElement("OtherEntities");
        
        root.appendChild(otherEntities);
        //Discover other entities available that this class doesn't already know about.
        
        //Get registered provider prefixes
        Set<String> providers = entityBroker.getRegisteredPrefixes();
        
        //Read the additional hidden providers, provide backward compatibily with that old property
        String[] txhiddenProviders = serverConfigurationService.getStrings("textarea.hiddenProviders");
        String[] ebhiddenProviders = serverConfigurationService.getStrings("entity-browser.hiddenProviders");
        //Combine txhiddenProviders, ebhiddenProviders and hiddenProviders
        if (txhiddenProviders != null)
        	Collections.addAll(hiddenProviders,txhiddenProviders);
        if (ebhiddenProviders != null)
        	Collections.addAll(hiddenProviders,ebhiddenProviders);
        
        for (String provider : providers) {
          // Check if this provider is hidden or not
          boolean skip = false;

          for (int i = 0; i < hiddenProviders.size(); i++) {
            if (provider.equals(hiddenProviders.get(i)))
              skip = true;
          }
          if (!skip) {
        	  //Find entities in the provider
        	  List<String> entities =
        			  entityBroker.findEntityRefs(new String[] { provider },
        					  new String[] { "context", "userId" }, new String[] { siteId, user }, true);
        	  if (entities != null && entities.size() > 0) {
        		  Element entityProvider=doc.createElement("EntityProvider");
        		  //Get the title from the local properties file
        		  String title = resourceLoader.getString("entitybrowser." + provider);
        		  if (title == null) {
        			  title = provider;
        		  }
        		  entityProvider.setAttribute("name", title);
        		  entityProvider.setAttribute("provider", provider);
        		  otherEntities.appendChild(entityProvider);
        		  //Does this need the children recursion of the ListProducer?
        		  for (String entity: entities) {
        			  Element entityItem = appendEntity(entity,doc);
        			  //This could go multiple levels but the original EBrowser only did 1 level
        			  List<String> childEntities = findChildren(entity, user);
        			  for (String childEntity: childEntities) {
        				  Element entityChild = appendEntity(childEntity,doc); 
        				  entityItem.appendChild(entityChild);
        			  }
        			  entityProvider.appendChild(entityItem);
        		  }
        	  }
          }
        }
    }
        
    private Element appendEntity(String entity, Document doc) {
    	Element element=doc.createElement("EntityItem");
    	String title = entityBroker.getPropertyValue(entity, "title");
    	element.setAttribute("url", entityBroker.getEntityURL(entity));
    	element.setAttribute("name",title);
    	element.setAttribute("size","0");
    	return element;
    }

    /**
     * Find the next level of decendent Entities for a given reference and user
     * 
     * @param reference
     * @param user
     * @return List of Entity References
     */
    private List<String> findChildren(String reference, String user) {
      return entityBroker
        .findEntityRefs(new String[] {entityBroker.getPropertyValue(reference, "child_provider")},
                        new String[] {"parentReference", "userId"}, 
                        new String[] {reference, user},
                        true);
    }
    
    private void getForumsAndThreads(String dir, Node root, Document doc, String type, ConnectorHelper ch) {

        String siteId = null;
     	String[] f = dir.split("/");
       	siteId = f[2];
    	
    	
    	Element msgForums=doc.createElement("MsgForums");
    	root.appendChild(msgForums);
    	List theseMsgForums = ch.getForumTopicReferences(siteId);
    	if(theseMsgForums==null){
    		return;
    	}

	SortedSet<Element> sortedForums = new TreeSet<Element>(new SortElementsForDisplay());

    	Iterator msgForumsIterator = theseMsgForums.iterator();
    	while(msgForumsIterator.hasNext()){
    		DecoratedForumInfo thisForumBean = (DecoratedForumInfo) ((EntityData) msgForumsIterator.next()).getData();

    		Element forumElement = doc.createElement("mForum");
    		forumElement.setAttribute("url", serverUrlPrefix + MFORUM_FORUM_PREFIX+String.valueOf(thisForumBean.getForumId()));
    		forumElement.setAttribute("name",thisForumBean.getForumTitle());

		SortedSet<Element> sortedTopics = new TreeSet<Element>(new SortElementsForDisplay());

    		Iterator<DecoratedTopicInfo> topicIterator = thisForumBean.getTopics().iterator();
    		while(topicIterator.hasNext()){
    			DecoratedTopicInfo thisTopicBean = topicIterator.next();

    			Element topicElement = doc.createElement("mTopic");
    			topicElement.setAttribute("url", serverUrlPrefix + MFORUM_TOPIC_PREFIX+String.valueOf(thisTopicBean.getTopicId()));
    			topicElement.setAttribute("name", thisTopicBean.getTopicTitle());

//			SortedSet<Element> sortedMessages = new TreeSet<Element>(new SortElementsForDisplay());
//
//    			Iterator thisMessageIterator = thisTopicBean.getMessages().iterator();
//    			while(thisMessageIterator.hasNext()){
//    				MessageBean thisMessageBean = (MessageBean) thisMessageIterator.next();
//    				Element messageElement = doc.createElement("mMessage");
//    				messageElement.setAttribute("url", serverUrlPrefix + MFORUM_MESSAGE_PREFIX+String.valueOf(thisMessageBean.getMessageId()));
//    				messageElement.setAttribute("name", thisMessageBean.getMessageLabel());
//    				sortedMessages.add(messageElement);
//    			}
//
//                	for (Element item: sortedMessages) {
//        	                topicElement.appendChild(item);
//	                }

    			sortedTopics.add(topicElement);
    		}

	        for (Element item: sortedTopics) {
        	        forumElement.appendChild(item);
        	}

    		sortedForums.add(forumElement);
    	}

	for (Element item: sortedForums) {
		msgForums.appendChild(item);
	}

    }
    
     
     
     
     
     
     private Node createCommonXml(Document doc,String commandStr, String type, String currentPath, String currentUrl ) {
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

     private String getAltReferenceRoot (String id)  {
          String altRoot = null;
          try {
               altRoot = StringUtil.trimToNull(contentHostingService.getProperties(id)
                    .getProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE));
          }
          catch (Exception e) {
               // do nothing, we either didn't have permission or the id is bogus
          }
          if (altRoot != null && !"/".equals(altRoot) && !"".equals(altRoot)) {
               if (!altRoot.startsWith(Entity.SEPARATOR))
                    altRoot = Entity.SEPARATOR + altRoot;
               if (altRoot.endsWith(Entity.SEPARATOR))
                    altRoot = altRoot.substring(0, altRoot.length() - Entity.SEPARATOR.length());
               return altRoot;
          }
          else
               return null;
     }

     private ContentResource bypassAddAttachment(HttpServletRequest request, String fileName, String mimeType, byte[] bytes, InputStream requestStream, ResourceProperties props) throws Exception {
    	 try {
    		 
             securityService.pushAdvisor(contentNewAdvisor);
	         String path = request.getPathInfo();
			 String resourceId = Validator.escapeResourceName(fileName);
			 String siteId = "";
			 if(path.contains("/user/")){
				 siteId = siteService.getSite("~" + path.replaceAll("\\/user\\/(.*)\\/", "$1")).getId();
			 } else {
				 siteId = siteService.getSite(path.replaceAll("\\/group\\/(.*)\\/", "$1")).getId();
}
			 String toolName = "fckeditor";
	    	 
             if (bytes != null) {
            	 return contentHostingService.addAttachmentResource(resourceId, siteId, toolName, mimeType, new ByteArrayInputStream(bytes), props);
             }
             else if (requestStream != null) {
            	 return contentHostingService.addAttachmentResource(resourceId, siteId, toolName, mimeType, requestStream, props);
             }
			 
    	 } catch (Exception ex) {
    		 throw ex;
    	 } finally {
             securityService.popAdvisor(contentNewAdvisor);
    	 }
    	 return null;
     }
     
     /**
      * This security advisor is used when making an assignment submission so that attachments can be added.
      * This copies content in assignment-tool/tool/src/java/org/sakaiproject/assignment/tool/AssignmentAction.java
      * @return The security advisor.
      */
     private SecurityAdvisor createSubmissionSecurityAdvisor() {
    	 return (userId, function, reference) -> {
    		 //Needed to be able to add or modify their own
    		 if (function.equals(contentHostingService.AUTH_RESOURCE_ADD) ||
    				 function.equals(contentHostingService.AUTH_RESOURCE_WRITE_OWN) ||
    				 function.equals(contentHostingService.AUTH_RESOURCE_HIDDEN)
    				 ) {
    			 return SecurityAdvisor.SecurityAdvice.ALLOWED;
    		 } else if (function.equals(contentHostingService.AUTH_RESOURCE_WRITE_ANY)) {
    			 M_log.info(userId + " requested ability to write to any content on "+ reference+
    					 " which we didn't expect, this should be investigated");
    			 return SecurityAdvisor.SecurityAdvice.ALLOWED;
    		 }
    		 return SecurityAdvisor.SecurityAdvice.PASS;
    	 };
     }
}
