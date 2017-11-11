/**
 * Copyright (c) 2005-2012 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.util;

public class SamigoExpressionFunctions {
    /**
     * calculate factorial of value
     * for example 5! = 5*4*3*2*1 = 120
     */
    static double factorial(double value) throws SamigoExpressionError
    {
        double res;
        int v = (int)value;
        
        if (value != v)
        {
          throw new SamigoExpressionError(400, "factorial");
        }
        
        res = v;
        v--;
        while (v > 1)
        {
            res *= v;
            v--;
        }

        if (res == 0) res = 1;        // 0! is per definition 1
        return res;
    }

    /**
     * calculate the modulus of the given values
     */
    static double modulus(double a, double b) throws SamigoExpressionError
    {
      // values must be integer
      int a_int = (int)a;
      int b_int = (int)b;
      if (a_int == a && b_int == b)
      {
        return a_int % b_int;
      }
      else
      {
        throw new SamigoExpressionError(400, "%");
      }
    }

    /**
     * calculate the sign of the given value
     */
    static double sign(double value)
    {
        if (value > 0) return 1;
        if (value < 0) return -1;
        return 0;
    }
}
