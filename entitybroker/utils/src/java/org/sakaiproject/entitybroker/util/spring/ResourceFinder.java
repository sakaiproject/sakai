/**
 * $Id$
 * $URL$
 * ResourceFinder.java - caching - May 29, 2008 11:59:02 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.util.spring;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * Takes a path or list of paths to resources and turns them into different things (file/IS/resource),
 * this also allows us to look on a relative or absolute path and will automatically
 * check the typical places one might expect to put sakai config files<br/>
 * Checks the environmental and file paths first and then defaults to checking the classloaders<br/>
 * Allows us to find resources in our pack since the Sakai context classloader is wrong,
 * too bad it is not correct, that would be cool, but it is wrong and it is not cool<br/>
 * <br/>
 * Sample usage:<xmp>
     <property name="configLocation">
         <bean class="org.sakaiproject.entitybroker.impl.util.ResourceFinder" factory-method="getResource">
             <constructor-arg value="ehcache.xml" />
         </bean>
     </property>
 * </xmp>
 * Just call whichever get* method you want to depending on the needed output<br/>
 * You can also get arrays of resources at once like so:<xmp>
     <property name="mappingLocations">
         <bean class="org.sakaiproject.entitybroker.impl.util.ResourceFinder"
             factory-method="getResources">
             <constructor-arg>
                 <list>
                     <value>org/sakaiproject/hierarchy/dao/hbm/HierarchyPersistentNode.hbm.xml</value>
                     <value>org/sakaiproject/hierarchy/dao/hbm/HierarchyNodeMetaData.hbm.xml</value>
                 </list>
             </constructor-arg>
         </bean>
     </property>
 * </xmp>
 * 
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@Slf4j
public class ResourceFinder {

   public static String relativePath = "sakai/";
   public static String environmentPathVariable = "sakai.home";

   private static List<Resource> makeResources(List<String> paths) {
      List<Resource> rs = new ArrayList<Resource>();
      if (paths != null && !paths.isEmpty()) {
         for (String path : paths) {
            try {
               Resource r = makeResource(path);
               rs.add(r);
            } catch (IllegalArgumentException e) {
               // do not add if not found, just skip
               log.warn(e.getMessage() + ", continuing...");
            }
         }
      }
      return rs;
   }

   private static Resource makeResource(String path) {
      if (path.startsWith("/")) {
         path = path.substring(1);
      }
      Resource r = null;
      // try the environment path first
      String envPath = getEnvironmentPath() + path;
      r = new FileSystemResource(envPath);
      if (! r.exists()) {
         // try the relative path next
         String relPath = getRelativePath() + path;
         r = new FileSystemResource(relPath);         
         if (! r.exists()) {
            // now try the classloader
            ClassLoader cl = ResourceFinder.class.getClassLoader();
            r = new ClassPathResource(path, cl);
            if (! r.exists()) {
               // finally try the system classloader
               cl = ClassLoader.getSystemClassLoader();
               r = new ClassPathResource(path, cl);
            }
         }
      }
      if (! r.exists()) {
         throw new IllegalArgumentException("Could not find this resource ("+path+") in any of the checked locations");
      }
      return r;
   }

   /**
    * Resolves a list of paths into resources relative to environmental defaults or relative paths or the classloader
    * @param paths a list of paths to resources (org/sakaiproject/mystuff/Thing.xml)
    * @return an array of Spring Resource objects
    */
   public static Resource[] getResources(List<String> paths) {
      return makeResources(paths).toArray(new Resource[paths.size()]);
   }

   public static File[] getFiles(List<String> paths) {
      List<Resource> rs = makeResources(paths);
      File[] files = new File[rs.size()];
      for (int i = 0; i < rs.size(); i++) {
         Resource r = rs.get(i);
         try {
            files[i] = r.getFile();
         } catch (IOException e) {
            throw new RuntimeException("Failed to get file for: " + r.getFilename(), e);
         }
      }
      return files;
   }

   public static InputStream[] getInputStreams(List<String> paths) {
      List<Resource> rs = makeResources(paths);
      InputStream[] streams = new InputStream[rs.size()];
      for (int i = 0; i < rs.size(); i++) {
         Resource r = rs.get(i);
         try {
            streams[i] = r.getInputStream();
         } catch (IOException e) {
            throw new RuntimeException("Failed to get inputstream for: " + r.getFilename(), e);
         }
      }
      return streams;
   }

   /**
    * Resolve a path into a resource relative to environmental defaults or relative paths or the classloader
    * @param path a path to a resource (org/sakaiproject/mystuff/Thing.xml)
    * @return the Spring Resource object
    */
   public static Resource getResource(String path) {
      return makeResource(path);
   }

   public static File getFile(String path) {
      Resource r = getResource(path);
      File f = null;
      try {
         f = r.getFile();
      } catch (IOException e) {
         throw new RuntimeException("Failed to get file for: " + r.getFilename(), e);
      }
      return f;
   }

   public static InputStream getInputStream(String path) {
      Resource r = getResource(path);
      InputStream is = null;
      try {
         is = r.getInputStream();
      } catch (IOException e) {
         throw new RuntimeException("Failed to get inputstream for: " + r.getFilename(), e);
      }
      return is;
   }

   protected static String getRelativePath() {
      File currentPath = new File("");
      File f = new File(currentPath, relativePath);
      if (! f.exists() || ! f.isDirectory()) {
         f = new File(currentPath, "sakai");
         if (! f.exists() || ! f.isDirectory()) {
            f = currentPath;
         }
      }
      String absPath = f.getAbsolutePath();
      if (! absPath.endsWith(File.separatorChar + "")) {
         absPath += File.separatorChar;
      }
      return absPath;
   }

   protected static String getEnvironmentPath() {
      String envPath = System.getenv(environmentPathVariable);
      if (envPath == null) {
         envPath = System.getProperty(environmentPathVariable);
         if (envPath == null) {
            String container = getContainerHome();
            if (container == null) {
               container = "";
            }
            envPath = container + File.separatorChar + "sakai" + File.separatorChar;
         }
      }
      return envPath;
   }

   protected static String getContainerHome() {
      String catalina = System.getProperty("catalina.base");
      if (catalina == null) {
         catalina = System.getProperty("catalina.home");
      }
      return catalina;
   }

}
