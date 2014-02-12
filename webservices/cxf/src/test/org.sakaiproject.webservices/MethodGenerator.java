package org.sakaiproject.webservices;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
        Class clazz = SakaiPortalLogin.class;
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


                Class[] types = method.getParameterTypes();
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
