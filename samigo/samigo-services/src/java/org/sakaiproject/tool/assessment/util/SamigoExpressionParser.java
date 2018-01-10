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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mariuszgromada.math.mxparser.Expression;
import org.sakaiproject.tool.assessment.services.GradingService;


public class SamigoExpressionParser
{

  public static String INFINITY = "Infinity";
  public static String NaN = "NaN";
  public static Pattern oldLogPattern;

  /**
   * finalructor.
   * Initializes all data with zeros and empty strings
   */
  public SamigoExpressionParser()
  {
    expr = "";
    expr_pos = -1;
    expr_c = '\0';

    token = "";
    token_type = TOKENTYPE.NOTHING;

    oldLogPattern = Pattern.compile("log\\([^,]*\\)");
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
      // mxParser wants "pi" not "PI"
      expr = new_expr.toLowerCase();
      // mxParser doesn't understand log(e) they do understand ln(e)
      Matcher matcher = oldLogPattern.matcher(expr);
      if (matcher.matches()) {
          expr = expr.replaceAll("log", "ln");
      }
      // mxParser doesn't understand SIGN they do understand SGN
      expr = expr.replaceAll("sign", "sgn");

      ans = BigDecimal.valueOf(0.0);

      // get the first character in expr
      getFirstChar();

      getToken();
      
      // check whether the given expression is empty
      if (token_type == TOKENTYPE.DELIMETER && expr_c == '\0')
      {
          throw new SamigoExpressionError(row(), col(), 4);
      }

      Expression e = null;
      try {
          e = new Expression(expr);
          double d = e.calculate();
          ans = new BigDecimal(d, MathContext.DECIMAL64);
      }
      catch (NumberFormatException nfe) {
          String errorMessage = e != null ? e.getErrorMessage() : expr;
          throw new SamigoExpressionError(401, errorMessage);
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


  /**
   * checks if the given char c is a minus
   */
  boolean isMinus(final char c)
  {
    return c == '-';
  }

  /**
   * checks if the given char c is whitespace
   * whitespace when space chr(32) or tab chr(9)
   */
  boolean isWhiteSpace(final char c)
  {
    return c == 32 || c == 9;  // space or tab
  }

  /**
   * checks if the given char c is a delimeter
   * minus is checked apart, can be unary minus
   */
  boolean isDelimeter(final char c)
  {
    return "&|<>=+/*%^!".indexOf(c) != -1;
  }

  /**
   * checks if the given char c is NO delimeter
   */
  boolean isNotDelimeter(final char c)
  {
    return "&|<>=+-/*%^!()".indexOf(c) != -1;
  }

  /**
   * checks if the given char c is a letter or undersquare
   */
  boolean isAlpha(final char c)
  {
    char cUpper = Character.toUpperCase(c);
    return "ABCDEFGHIJKLMNOPQRSTUVWXYZ_".indexOf(cUpper) != -1;
  }

  /**
   * checks if the given char c is a digit or dot
   */
  boolean isDigitDot(final char c)
  {
    return "0123456789.".indexOf(c) != -1;
  }

  /**
   * checks if the given char c is a digit
   */
  boolean isDigit(final char c)
  {
    return "0123456789".indexOf(c) != -1;
  }

  /**
   * checks if the given variable name is legal to use, i.e. not
   * equal to "pi", "e", etc.
   */
  boolean isLegalVariableName(String name)
  {
    String nameUpper = name.toUpperCase();
    if (nameUpper.equals("E")) return false;
    if (nameUpper.equals("PI")) return false;

    return true;
  }

  /**
   * Get the next character from the expression.
   * The character is stored into the char expr_c.
   * If the end of the expression is reached, the function puts zero ('\0')
   * in expr_c.
   */
  void getChar()
  {
    expr_pos++;
    if (expr_pos < expr.length())
    {
      expr_c = expr.charAt(expr_pos);
    }
    else
    {
      expr_c = '\0';
    }
  }

  /**
   * Get the first character from the expression.
   * The character is stored into the char expr_c.
   * If the end of the expression is reached, the function puts zero ('\0')
   * in expr_c.
   */
  void getFirstChar()
  {
    expr_pos = 0;
    if (expr_pos < expr.length())
    {
      expr_c = expr.charAt(expr_pos);
    }
    else
    {
      expr_c = '\0';
    }
  }

  /***
   * Get next token in the current string expr.
   * Uses the Parser data expr, e, token, t, token_type and err
   */
  void getToken() throws SamigoExpressionError
  {
    token_type = TOKENTYPE.NOTHING;
    token = "";     // set token empty

    // skip over whitespaces
    while (isWhiteSpace(expr_c))     // space or tab
    {
      getChar();
    }

    // check for end of expression
    if (expr_c == '\0')
    {
      // token is empty
      token_type = TOKENTYPE.DELIMETER;
      return;
    }

    // check for minus
    if (expr_c == '-')
    {
      token_type = TOKENTYPE.DELIMETER;
      token += expr_c;
      getChar();
      return;
    }

    // check for parentheses
    if (expr_c == '(' || expr_c == ')')
    {
      token_type = TOKENTYPE.DELIMETER;
      token += expr_c;
      getChar();
      return;
    }

    // check for operators (delimeters)
    if (isDelimeter(expr_c))
    {
      token_type = TOKENTYPE.DELIMETER;
      while (isDelimeter(expr_c))
      {
        token += expr_c;
        getChar();
      }
      return;
    }

    // check for a value
    if (isDigitDot(expr_c))
    {
      token_type = TOKENTYPE.NUMBER;
      while (isDigitDot(expr_c))
      {
        token += expr_c;
        getChar();
      }

      // check for scientific notation like "2.3e-4" or "1.23e50"
      if (expr_c == 'e' || expr_c == 'E')
      {
        token += expr_c;
        getChar();

        if (expr_c == '+' || expr_c == '-')
        {
          token += expr_c;
          getChar();
        }

        while (isDigit(expr_c))
        {
          token += expr_c;
          getChar();
        }
      }

      return;
    }

    // check for variables or functions
    if (isAlpha(expr_c))
    {
      while (isAlpha(expr_c) || isDigit(expr_c))
      {
        token += expr_c;
        getChar();
      }
      
      // skip whitespaces
      while (isWhiteSpace(expr_c)) // space or tab
      {
        getChar();
      }

      // check the next non-whitespace character
      if (expr_c == '(')
      {
        token_type = TOKENTYPE.FUNCTION;
      }
      else
      {
        token_type = TOKENTYPE.VARIABLE;
      }
      
      return;
    }


    // something unknown is found, wrong characters -> a syntax error
    token_type = TOKENTYPE.UNKNOWN;
    while (expr_c != '\0')
    {
      token += expr_c;
      getChar();
    }

    throw new SamigoExpressionError(row(), col(), 1, token);
  }

  /**
   * Shortcut for getting the current row value (one based)
   * Returns the line of the currently handled expression
   */
  int row()
  {
    return -1;
  }

  /**
   * Shortcut for getting the current col value (one based)
   * Returns the column (position) where the last token starts
   */
  int col()
  {
    return expr_pos - token.length() + 1;
  }

/// private enumerations
  private enum TOKENTYPE {NOTHING, DELIMETER, NUMBER, VARIABLE, FUNCTION, UNKNOWN}

  /// private data
  private String expr;          /// holds the expression
  private int expr_pos;         /// points to the current position in expr
  private char expr_c;          /// holds the current character from expr

  private String token;         /// holds the token
  private TOKENTYPE token_type; /// type of the token

  private BigDecimal ans;           /// holds the result of the expression
  private String ans_str;       /// holds a string containing the result
                                /// of the expression

  /// list with variables defined by user
  private Map<String, BigDecimal> user_var = new HashMap<String, BigDecimal>(); 
}
