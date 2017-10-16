/**
 * Copyright (c) 2007-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational
* Community License, Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.hierarchy.dao;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


/**
 * Allows us to find resources in our pack since the Sakai context classloader is wrong,
 * too bad it is not correct, that would be cool, but it is wrong and it is not cool<br/>
 * Takes a list of paths to resources and turns them into 
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ResourceFinder {

   private static List<Resource> makeResources(List<String> paths) {
      List<Resource> rs = new ArrayList<Resource>();
      if (paths != null && !paths.isEmpty()) {
         ClassLoader cl = ResourceFinder.class.getClassLoader();
         for (String path : paths) {
            Resource r = new ClassPathResource(path, cl);
            if (r.exists()) {
               rs.add(r);
            }
         }
      }
      return rs;
   }

   /**
    * Resolves a list of paths into resources within the current classloader
    * @param paths a list of paths to resources (org/sakaiproject/mystuff/Thing.xml)
    * @return an array of Spring Resource objects
    */
   public static Resource[] getResources(List<String> paths) {
      return makeResources(paths).toArray(new Resource[] {});
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

}
