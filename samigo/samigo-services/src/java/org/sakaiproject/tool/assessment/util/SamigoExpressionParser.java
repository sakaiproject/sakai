/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;

import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.mXparser;
import org.sakaiproject.tool.assessment.services.GradingService;


public class SamigoExpressionParser
{

  public static String INFINITY = "Infinity";
  public static String NaN = "NaN";

  // Samigo once had a custom function parser. Now we use a well-supported library called mxParser that is case-sensitive.
  public static String[] oldSamigoParserVars = {"SIN", "COS", "TAN", "ASIN", "ACOS", "ATAN", "ABS", "EXP", "SGN", "SQRT", "LOG10", "LN", "PI", "E", "SIGN", "LOG", "log"};
  public static String[] newSamigoParserVars = {"sin", "cos", "tan", "asin", "acos", "atan", "abs", "exp", "sgn", "sqrt", "log10", "ln", "pi", "e", "sgn",  "ln",  "ln"};

  // Special case for factorial replacement
  public static String OLD_FACTORIAL_PATTERN = "(?i)factorial\\((\\d+)\\)";
  public static String NEW_FACTORIAL_PATTERN = "($1!)";

  /**
   * finalructor.
   * Initializes all data with zeros and empty strings
   */
  public SamigoExpressionParser()
  {
    expr = "";
    mXparser.setEpsilon(1.0E-99);
  }

  /**
   * parses and evaluates the given expression
   * On error, an error of type Error is thrown
   */
  public String parse(final String new_expr) throws SamigoExpressionError {
      return parse(new_expr, 5);
  }

  /**
   * parses and evaluates the given expression
   * On error, an error of type Error is thrown
   */
  public String parse(final String new_expr, final int decimals) throws SamigoExpressionError
  {
    try
    {
      expr = new_expr.trim();
      // mxParser doesn't understand log(e) they do understand ln(e)
      // mxParser wants "pi" not "PI"
      final int cnt = oldSamigoParserVars.length;
      for (int i = 0; i < cnt; i++) {
    	  // Only match whole words, e.g., don't do a PI/pi replace on a variable called "applePies"
    	  expr = expr.replaceAll("\\b" + oldSamigoParserVars[i] + "\\b", newSamigoParserVars[i]);
      }

      // Also look for the factorial pattern
      expr = expr.replaceAll(OLD_FACTORIAL_PATTERN, NEW_FACTORIAL_PATTERN);

      ans = BigDecimal.valueOf(0.0);

      Expression e = null;
      try {
          e = new Expression(expr);
          if (expr.contains("E")) {
              mXparser.disableUlpRounding();
          }
          double d = e.calculate();
          ans = new BigDecimal(d, MathContext.DECIMAL64);
      }
      catch (NumberFormatException nfe) {
          String errorMessage = e != null ? e.getErrorMessage() : expr;
          throw new SamigoExpressionError(401, errorMessage);
      }
      finally {
          mXparser.enableUlpRounding();
      }

      GradingService service = new GradingService();
      ans_str = service.toScientificNotation(ans.toPlainString(), decimals);

      // add the answer to memory as variable "Ans"
      user_var.put("ANS", new BigDecimal(ans_str));
    }
    catch (SamigoExpressionError err)
    {
      ans_str = err.get();
      throw err;
    }

    return ans_str;
  }

/// private data
  private String expr;          /// holds the expression
  private BigDecimal ans;           /// holds the result of the expression
  private String ans_str;       /// holds a string containing the result
                                /// of the expression

  /// list with variables defined by user
  private Map<String, BigDecimal> user_var = new HashMap<String, BigDecimal>(); 
}
