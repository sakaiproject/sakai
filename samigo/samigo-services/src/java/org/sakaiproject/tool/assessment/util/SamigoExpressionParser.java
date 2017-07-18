/***********************************************************************************
 * Found this code here: http://www.speqmath.com/tutorials/expression_parser_java/
 * I believe it's free and open. I didn't see where it said otherwise.
 */
/***********************************************************************************
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

package org.sakaiproject.tool.assessment.util;

import java.math.BigDecimal;
import java.util.*;

import org.sakaiproject.tool.assessment.services.GradingService;


public class SamigoExpressionParser
{

  public static String INFINITY = "Infinity";
  public static String NaN = "NaN";

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
      // initialize all variables
      expr = new_expr;     // copy the given expression to expr
      ans = BigDecimal.valueOf(0.0);

      // get the first character in expr
      getFirstChar();

      getToken();
      
      // check whether the given expression is empty
      if (token_type == TOKENTYPE.DELIMETER && expr_c == '\0')
      {
          throw new SamigoExpressionError(row(), col(), 4);
      }

      // infinity and NaN cannot be processed correctly
      try {
    	  ans = parse_level1();
      }
      catch (NumberFormatException e) {
          throw new SamigoExpressionError(402, expr);
      }

      // check for garbage at the end of the expression
      if (token_type != TOKENTYPE.DELIMETER || token.length() > 0)
      {
        if (token_type == TOKENTYPE.DELIMETER)
        {
          // user entered a not existing operator like "//"
          throw new SamigoExpressionError(row(), col(), 101, token);
        }
        else
        {
          throw new SamigoExpressionError(row(), col(), 5, token);
        }
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
   * assignment of variable or function
   */
  BigDecimal parse_level1() throws SamigoExpressionError
  {
    if (token_type == TOKENTYPE.VARIABLE)
    {
      // skip whitespaces
      while (isWhiteSpace(expr_c)) // space or tab
      {
        getChar();
      }

      // check the next non-whitespace character
      if (expr_c == '=')
      {
        String var_name = token;
        
        // get the token '='
        getToken();
        
        // assignment
        BigDecimal ans;
        getToken();
        ans = parse_level2();
        
        // check whether the token is a legal name
        if (isLegalVariableName(var_name))
        {
          user_var.put(var_name.toUpperCase(), ans);
        }
        else
        {
          throw new SamigoExpressionError(row(), col(), 300);
        }
        return ans;
      }
    }

    return parse_level2();
  }


  /**
   * conditional operators and bitshift
   */
  BigDecimal parse_level2() throws SamigoExpressionError
  {
    OPERATOR op_id;
    BigDecimal ans;
    ans = parse_level3();

    op_id = get_operator_id(token);
    while (op_id == OPERATOR.AND || 
           op_id == OPERATOR.OR || 
           op_id == OPERATOR.BITSHIFTLEFT || 
           op_id == OPERATOR.BITSHIFTRIGHT)
    {
      getToken();
      ans = eval_operator(op_id, ans, parse_level3());
      op_id = get_operator_id(token);
    }

    return ans;
  }

  /**
   * conditional operators
   */
  BigDecimal parse_level3() throws SamigoExpressionError
  {
    OPERATOR op_id;
    BigDecimal ans;
    ans = parse_level4();

    op_id = get_operator_id(token);
    while (op_id == OPERATOR.EQUAL || 
           op_id == OPERATOR.UNEQUAL || 
           op_id == OPERATOR.SMALLER || 
           op_id == OPERATOR.LARGER || 
           op_id == OPERATOR.SMALLEREQ || 
           op_id == OPERATOR.LARGEREQ)
    {
      getToken();
      ans = eval_operator(op_id, ans, parse_level4());
      op_id = get_operator_id(token);
    }

    return ans;
  }

  /**
   * add or subtract
   */
  BigDecimal parse_level4() throws SamigoExpressionError
  {
    OPERATOR op_id;
    BigDecimal ans;
    ans = parse_level5();

    op_id = get_operator_id(token);
    while (op_id == OPERATOR.PLUS || 
           op_id == OPERATOR.MINUS)
    {
      getToken();
      ans = eval_operator(op_id, ans, parse_level5());
      op_id = get_operator_id(token);
    }

    return ans;
  }


  /**
   * multiply, divide, modulus, xor
   */
  BigDecimal parse_level5() throws SamigoExpressionError
  {
    OPERATOR op_id;
    BigDecimal ans;
    ans = parse_level6();

    op_id = get_operator_id(token);
    while (op_id == OPERATOR.MULTIPLY || 
           op_id == OPERATOR.DIVIDE || 
           op_id == OPERATOR.MODULUS || 
           op_id == OPERATOR.XOR)
    {
      getToken();
      ans = eval_operator(op_id, ans, parse_level6());
      op_id = get_operator_id(token);
    }

    return ans;
  }


  /**
   * power
   */
  BigDecimal parse_level6() throws SamigoExpressionError
  {
    OPERATOR op_id;
    BigDecimal ans;
    ans = parse_level7();

    op_id = get_operator_id(token);
    while (op_id == OPERATOR.POW)
    {
      getToken();
      ans = eval_operator(op_id, ans, parse_level7());
      op_id = get_operator_id(token);
    }

    return ans;
  }

  /**
   * Factorial
   */
  BigDecimal parse_level7() throws SamigoExpressionError
  {
    OPERATOR op_id;
    BigDecimal ans;
    ans = parse_level8();

    op_id = get_operator_id(token);
    while (op_id == OPERATOR.FACTORIAL)
    {
      getToken();
      // factorial does not need a value right from the
      // operator, so zero is filled in.
      ans = eval_operator(op_id, ans, BigDecimal.valueOf(0.0));
      op_id = get_operator_id(token);
    }

    return ans;
  }

  /**
   * Unary minus
   */
  BigDecimal parse_level8() throws SamigoExpressionError
  {
    BigDecimal ans;

    OPERATOR op_id = get_operator_id(token);
    if (op_id == OPERATOR.MINUS)
    {
      getToken();
      ans = parse_level9();
      ans = ans.negate();
    }
    else
    {
      ans = parse_level9();
    }

    return ans;
  }


  /**
   * functions
   */
  BigDecimal parse_level9() throws SamigoExpressionError
  {
    String fn_name;
    BigDecimal ans;

    if (token_type == TOKENTYPE.FUNCTION)
    {
      fn_name = token;
      getToken();
      ans = eval_function(fn_name, parse_level10());
    }
    else
    {
      ans = parse_level10();
    }

    return ans;
  }


  /**
   * parenthesized expression or value
   */
  BigDecimal parse_level10() throws SamigoExpressionError
  {
    // check if it is a parenthesized expression
    if (token_type == TOKENTYPE.DELIMETER)
    {
      if (token.equals("("))
      {
        getToken();
        BigDecimal ans = parse_level2();
        if (token_type != TOKENTYPE.DELIMETER || !token.equals(")"))
        {
          throw new SamigoExpressionError(row(), col(), 3);
        }
        getToken();
        return ans;
      }
    }

    // if not parenthesized then the expression is a value
    return parse_number();
  }


  BigDecimal parse_number() throws SamigoExpressionError
  {
    BigDecimal ans = BigDecimal.valueOf(0.0);

    switch (token_type)
    {
      case NUMBER:
        // this is a number
        ans = new BigDecimal(token);
        getToken();
        break;

      case VARIABLE:
        // this is a variable
        ans = eval_variable(token);
        getToken();
        break;

      default:
        // syntax error or unexpected end of expression
        if (token.length() == 0)
        {
          throw new SamigoExpressionError(row(), col(), 6);
        }
        else
        {
          throw new SamigoExpressionError(row(), col(), 7);
        }
    }

    return ans;
  }


  /**
   * returns the id of the given operator
   * treturns -1 if the operator is not recognized
   */
  OPERATOR get_operator_id(final String op_name)
  {
    // level 2
    if (op_name.equals("&")) {return OPERATOR.AND;}
    if (op_name.equals("|")) {return OPERATOR.OR;}
    if (op_name.equals("<<")) {return OPERATOR.BITSHIFTLEFT;}
    if (op_name.equals(">>")) {return OPERATOR.BITSHIFTRIGHT;}

    // level 3
    if (op_name.equals("=")) {return OPERATOR.EQUAL;}
    if (op_name.equals("<>")) {return OPERATOR.UNEQUAL;}
    if (op_name.equals("<")) {return OPERATOR.SMALLER;}
    if (op_name.equals(">")) {return OPERATOR.LARGER;}
    if (op_name.equals("<=")) {return OPERATOR.SMALLEREQ;}
    if (op_name.equals(">=")) {return OPERATOR.LARGEREQ;}

    // level 4
    if (op_name.equals("+")) {return OPERATOR.PLUS;}
    if (op_name.equals("-")) {return OPERATOR.MINUS;}

    // level 5
    if (op_name.equals("*")) {return OPERATOR.MULTIPLY;}
    if (op_name.equals("/")) {return OPERATOR.DIVIDE;}
    if (op_name.equals("%")) {return OPERATOR.MODULUS;}
    if (op_name.equals("||")) {return OPERATOR.XOR;}

    // level 6
    if (op_name.equals("^")) {return OPERATOR.POW;}

    // level 7
    if (op_name.equals("!")) {return OPERATOR.FACTORIAL;}

    return OPERATOR.UNKNOWN;
  }


  /**
   * evaluate an operator for given valuess
   */
  BigDecimal eval_operator(final OPERATOR op_id, final BigDecimal bdlhs, final BigDecimal bdrhs) throws SamigoExpressionError
  {

  double lhs = bdlhs.doubleValue();
  double rhs = bdrhs.doubleValue();

  switch (op_id)
    {
      // level 2
      case AND:           return BigDecimal.valueOf((int)lhs & (int)rhs);
      case OR:            return BigDecimal.valueOf((int)lhs | (int)rhs);
      case BITSHIFTLEFT:  return BigDecimal.valueOf((int)lhs << (int)rhs);
      case BITSHIFTRIGHT: return BigDecimal.valueOf((int)lhs >> (int)rhs);

      // level 3
      case EQUAL:     return BigDecimal.valueOf((bdlhs.compareTo(bdrhs) == 0 ) ? 1 : 0);
      case UNEQUAL:   return BigDecimal.valueOf((bdlhs.compareTo(bdrhs) != 0 ) ? 1 : 0);
      case SMALLER:   return BigDecimal.valueOf((bdlhs.compareTo(bdrhs) < 0 )  ? 1 : 0);
      case LARGER:    return BigDecimal.valueOf((bdlhs.compareTo(bdrhs) > 0 )  ? 1 : 0);
      case SMALLEREQ: return BigDecimal.valueOf((bdlhs.compareTo(bdrhs) <= 0 ) ? 1 : 0);
      case LARGEREQ:  return BigDecimal.valueOf((bdlhs.compareTo(bdrhs) >= 0 ) ? 1 : 0);

      // level 4
      case PLUS:      return bdlhs.add(bdrhs);
      case MINUS:     return bdlhs.subtract(bdrhs);

      // level 5
      case MULTIPLY:  return bdlhs.multiply(bdrhs);
      case DIVIDE:    return bdlhs.divide(bdrhs);

      case MODULUS:   return BigDecimal.valueOf(SamigoExpressionFunctions.modulus(lhs, rhs));
      case XOR:       return BigDecimal.valueOf((int)lhs ^ (int)rhs);

      // level 6
      case POW:       return BigDecimal.valueOf(Math.pow(lhs, rhs));

      // level 7
      case FACTORIAL: return BigDecimal.valueOf(SamigoExpressionFunctions.factorial(lhs));
    }

    throw new SamigoExpressionError(row(), col(), 104);
  }


  /**
   * evaluate a function
   */
  BigDecimal eval_function(final String fn_name, final BigDecimal bdvalue) throws SamigoExpressionError
  {
    // first make the function name upper case
    String fnUpper = fn_name.toUpperCase();

    // arithmetic
    if (fnUpper.equals("ABS"))   {return bdvalue.abs();}
    
    Double value = bdvalue.doubleValue();
    Double retValue = null;
    //The rest of these BigDecimal can't do directly
    if (fnUpper.equals("EXP"))   {retValue = Math.exp(value);}
    if (fnUpper.equals("SIGN"))  {retValue =  SamigoExpressionFunctions.sign(value);}
    if (fnUpper.equals("SQRT"))  {retValue =  Math.sqrt(value);}
    if (fnUpper.equals("LOG") 
        || fnUpper.equals("LN")) {retValue =  Math.log(value);}
    if (fnUpper.equals("LOG10")) {retValue =  Math.log10(value);}

    // trigonometric
    if (fnUpper.equals("SIN"))   {retValue =  Math.sin(value);}
    if (fnUpper.equals("COS"))   {retValue =  Math.cos(value);}
    if (fnUpper.equals("TAN"))   {retValue =  Math.tan(value);}
    if (fnUpper.equals("ASIN"))  {retValue =  Math.asin(value);}
    if (fnUpper.equals("ACOS"))  {retValue =  Math.acos(value);}
    if (fnUpper.equals("ATAN"))  {retValue =  Math.atan(value);}

    // probability
    if (fnUpper.equals("FACTORIAL")) {retValue = SamigoExpressionFunctions.factorial(value);}

    if (retValue != null) {
    	return BigDecimal.valueOf(retValue);
    }
    // unknown function
    throw new SamigoExpressionError(row(), col(), 102, fn_name);
  }


  /**
   * evaluate a variable
   */
  BigDecimal eval_variable(final String var_name) throws SamigoExpressionError
  {
    // first make the variable name uppercase
    String varUpper = var_name.toUpperCase();

    // check for built-in variables
    if (varUpper.equals("E"))  {return BigDecimal.valueOf(Math.E);}
    if (varUpper.equals("PI")) {return BigDecimal.valueOf(Math.PI);}

    // check for user defined variables
    if (user_var.containsKey(varUpper))
    {
      BigDecimal ans = user_var.get(varUpper);
      return ans;
    }

    // unknown variable
    throw new SamigoExpressionError(row(), col(), 103, var_name);
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

  private enum OPERATOR {UNKNOWN, 
                 AND, OR, BITSHIFTLEFT, BITSHIFTRIGHT,         // level 2
                 EQUAL, UNEQUAL, SMALLER, LARGER, SMALLEREQ, LARGEREQ, // level 3
                 PLUS, MINUS,                     // level 4
                 MULTIPLY, DIVIDE, MODULUS, XOR,  // level 5
                 POW,                             // level 6
                 FACTORIAL}                       // level 7

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