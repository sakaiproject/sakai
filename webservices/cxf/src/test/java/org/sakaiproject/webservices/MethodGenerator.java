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

import java.lang.reflect.Method;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import org.sakaiproject.webservices.SakaiPortalLogin;

/**
 * Created by jbush on 2/11/14.
 */

/**
 * helpful utility for migrating from axis, this guy will output the method signatures
 * for a class once we've brought the jws file forward, so its just a copy and past
 * affair to get all the annotations in place.
 */
@Slf4j
public class MethodGenerator {

    @Test
    public void generate() {
        Class<SakaiPortalLogin> clazz = SakaiPortalLogin.class;
        for (Method method : clazz.getMethods()) {
            if (!method.getDeclaringClass().getName().equals(clazz.getName())) {
                continue;
            }
            log.info("@WebMethod");
            log.info("@Path(\"/" + method.getName() + "\")");
            log.info("@Produces(\"text/plain\")");
            log.info("@GET");
            log.info("public " + method.getReturnType().getSimpleName() + " " + method.getName() + "(");
            Paranamer paranamer = new BytecodeReadingParanamer();

            try {
                String[] parameterNames = paranamer.lookupParameterNames(method);


                Class<?>[] types = method.getParameterTypes();
                int i = 0;
                for (String name : parameterNames) {
                    log.info("@WebParam(name = \"" + name + "\", partName = \"" + name + "\") @QueryParam(\"" + name + "\") " + types[i].getSimpleName() + " " + name);
                    if (i + 1 != parameterNames.length) {
                        log.info(",");
                    } else {
                        log.info(") {\n");
                    }
                    i++;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }
    }
}
