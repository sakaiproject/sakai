/**
 * Copyright (c) 2005 The Apereo Foundation
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
package org.sakaiproject.webservices;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import org.junit.Test;
import org.sakaiproject.webservices.SakaiPortalLogin;

import java.lang.reflect.Method;

/**
 * Created by jbush on 2/11/14.
 */

/**
 * helpful utility for migrating from axis, this guy will output the method signatures
 * for a class once we've brought the jws file forward, so its just a copy and past
 * affair to get all the annotations in place.
 */
public class MethodGenerator {

    @Test
    public void generate() {
        Class<SakaiPortalLogin> clazz = SakaiPortalLogin.class;
        for (Method method : clazz.getMethods()) {
            if (!method.getDeclaringClass().getName().equals(clazz.getName())) {
                continue;
            }
            System.out.println("@WebMethod");
            System.out.println("@Path(\"/" + method.getName() + "\")");
            System.out.println("@Produces(\"text/plain\")");
            System.out.println("@GET");
            System.out.println("public " + method.getReturnType().getSimpleName() + " " + method.getName() + "(");
            Paranamer paranamer = new BytecodeReadingParanamer();

            try {
                String[] parameterNames = paranamer.lookupParameterNames(method);


                Class<?>[] types = method.getParameterTypes();
                int i = 0;
                for (String name : parameterNames) {
                    System.out.print("@WebParam(name = \"" + name + "\", partName = \"" + name + "\") @QueryParam(\"" + name + "\") " + types[i].getSimpleName() + " " + name);
                    if (i + 1 != parameterNames.length) {
                        System.out.println(",");
                    } else {
                        System.out.println(") {\n");
                    }
                    i++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
