package org.sakaiproject.webservices;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * Created by jbush on 2/11/14.
 */

public class MethodGenerator {

    @Test
    public void generate() {
        Class clazz = SakaiScript.class;
        for (Method method : clazz.getMethods() ){
            System.out.println("@WebMethod");
            System.out.println("public " +  method.getName() + "(");
            Paranamer paranamer = new BytecodeReadingParanamer();

            String[] parameterNames = paranamer.lookupParameterNames(method);
            Class[] types = method.getParameterTypes();
            int i=0;
            for (String name : parameterNames) {
                System.out.print("@WebParam(name = \"" + name + "\", partName = \"" + name + "\") " + types[i].getSimpleName());
                if (i + 1 != parameterNames.length) {
                    System.out.println(",");
                } else {
                    System.out.println(") {");
                }
                i++;
            }

        }
    }
}
