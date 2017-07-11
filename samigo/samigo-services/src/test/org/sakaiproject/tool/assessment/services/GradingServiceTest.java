/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.util.SamigoExpressionError;

public class GradingServiceTest {
    GradingService gradingService;

    String sampleItemText1 = "Alice has {A} apples, Bob has {B} apples, and Charles has {C} apples. Of the total apples among the group, what percent are Bob's? {{SOLUTION}}";
    String sampleSolutionFormula = "({B} / ({A} + {B} + {C}))*100";

    String sampleItemText1WithCalc = "Alice has {A} apples, Bob has {B} apples, and Charles has {C} apples. Of the [[{A} + {B} + {C}]] apples among the group, {{SOLUTION}} percent are Bob's.";

    String sampleItemText2 = "Sample (1) {var1} * {var2} + {var3} = {{formula1}}, and also {var1} + {var2} - {var3} = {{formula2}}, more [random] text";
    String sampleFormula2a = "{var1} * {var2} + {var3}";
    String sampleFormula2b = "{var1 + {var2} - {var3}";

    String sampleItemText3 = "{var0} Sample (1) {var1} + {var2} + {var3} = {{formula1}}, calc=[[{var0}+{var1}+{var2}+{var3}+{var4}]] more [random] text, dblvar1=[[{var1}*2]] f2={{{formula2}}} and c3=[[[{var2}]]] and not calc=[[plain text]] and {var4}";
    String sampleFormula3 = "{var1}+{var2}+{var3}";

    @Before
    public void onSetUp() throws Exception {
        gradingService = new GradingService();
    }

    // CALCULATED_QUESTION tests
    @Test
    public void testMatchPatterns() {
        List<String> results = null;

        results = gradingService.extractVariables(sampleItemText1);
        Assert.assertNotNull(results);
        Assert.assertEquals(3, results.size());
        Assert.assertEquals("A", results.get(0));
        Assert.assertEquals("B", results.get(1));
        Assert.assertEquals("C", results.get(2));
        results = gradingService.extractFormulas(sampleItemText1);
        Assert.assertNotNull(results);
        Assert.assertEquals("SOLUTION", results.get(0));
        Assert.assertEquals(1, results.size());
        results = gradingService.extractCalculations(sampleItemText1);
        Assert.assertNotNull(results);
        Assert.assertEquals(0, results.size());

        results = gradingService.extractVariables(sampleItemText2);
        Assert.assertNotNull(results);
        Assert.assertEquals(3, results.size());
        Assert.assertEquals("var1", results.get(0));
        Assert.assertEquals("var2", results.get(1));
        Assert.assertEquals("var3", results.get(2));
        results = gradingService.extractFormulas(sampleItemText2);
        Assert.assertNotNull(results);
        Assert.assertEquals(2, results.size());
        Assert.assertEquals("formula1", results.get(0));
        Assert.assertEquals("formula2", results.get(1));
        results = gradingService.extractCalculations(sampleItemText2);
        Assert.assertNotNull(results);
        Assert.assertEquals(0, results.size());

        results = gradingService.extractVariables(sampleItemText1WithCalc);
        Assert.assertNotNull(results);
        Assert.assertEquals(3, results.size());
        Assert.assertEquals("A", results.get(0));
        Assert.assertEquals("B", results.get(1));
        Assert.assertEquals("C", results.get(2));
        results = gradingService.extractFormulas(sampleItemText1WithCalc);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("SOLUTION", results.get(0));
        results = gradingService.extractCalculations(sampleItemText1WithCalc);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("{A} + {B} + {C}", results.get(0));

        results = gradingService.extractVariables(sampleItemText3);
        Assert.assertNotNull(results);
        Assert.assertEquals(5, results.size());
        Assert.assertEquals("var0", results.get(0));
        Assert.assertEquals("var1", results.get(1));
        Assert.assertEquals("var2", results.get(2));
        Assert.assertEquals("var3", results.get(3));
        Assert.assertEquals("var4", results.get(4));
        results = gradingService.extractFormulas(sampleItemText3);
        Assert.assertNotNull(results);
        Assert.assertEquals(2, results.size());
        Assert.assertEquals("formula1", results.get(0));
        Assert.assertEquals("formula2", results.get(1));
        results = gradingService.extractCalculations(sampleItemText3);
        Assert.assertNotNull(results);
        Assert.assertEquals(3, results.size());
        Assert.assertEquals("{var0}+{var1}+{var2}+{var3}+{var4}", results.get(0));
        Assert.assertEquals("{var1}*2", results.get(1));
        Assert.assertEquals("{var2}", results.get(2));

    }

    @Test
    public void testExtractVariablesFormulas() {
        List<String> results = null;
        String text = null;

        text = "{A}+{B}={{D}} Hint: [[{A}-{B}]]";
        results = gradingService.extractVariables(text);
        Assert.assertNotNull(results);
        Assert.assertEquals(2, results.size());
        Assert.assertEquals("A", results.get(0));
        Assert.assertEquals("B", results.get(1));
        results = gradingService.extractFormulas(text);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("D", results.get(0));

        text = "{aaa11}+{bbb}={{dAnSw_3r}} Hint: [[{aaa11}-{bbb}]]";
        results = gradingService.extractVariables(text);
        Assert.assertNotNull(results);
        Assert.assertEquals(2, results.size());
        Assert.assertEquals("aaa11", results.get(0));
        Assert.assertEquals("bbb", results.get(1));
        results = gradingService.extractFormulas(text);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("dAnSw_3r", results.get(0));

        text = "{A} + {B} = {{D}} Hint: [[ {A} + {B} ]]";
        results = gradingService.extractVariables(text);
        Assert.assertNotNull(results);
        Assert.assertEquals(2, results.size());
        Assert.assertEquals("A", results.get(0));
        Assert.assertEquals("B", results.get(1));
        results = gradingService.extractFormulas(text);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("D", results.get(0));

        text = "{A}*{B}={{D}} Hint: [[{A}*{B}]]";
        results = gradingService.extractVariables(text);
        Assert.assertNotNull(results);
        Assert.assertEquals(2, results.size());
        Assert.assertEquals("A", results.get(0));
        Assert.assertEquals("B", results.get(1));
        results = gradingService.extractFormulas(text);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("D", results.get(0));

        text = "{A}-{B}={{D}} Hint: [[{A}-{B}]]";
        results = gradingService.extractVariables(text);
        Assert.assertNotNull(results);
        Assert.assertEquals(2, results.size());
        Assert.assertEquals("A", results.get(0));
        Assert.assertEquals("B", results.get(1));
        results = gradingService.extractFormulas(text);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("D", results.get(0));

        text = "{A}/{B}={{D}} Hint: [[{A}/{B}]]";
        results = gradingService.extractVariables(text);
        Assert.assertNotNull(results);
        Assert.assertEquals(2, results.size());
        Assert.assertEquals("A", results.get(0));
        Assert.assertEquals("B", results.get(1));
        results = gradingService.extractFormulas(text);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("D", results.get(0));

        // single variable
        text = "{A}={{D}} Hint: [[{A}]]";
        results = gradingService.extractVariables(text);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("A", results.get(0));
        results = gradingService.extractFormulas(text);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("D", results.get(0));

        // 3 variables
        text = "{A}+{B}+{C}={{D}} Hint: [[{A}+{B}+{C}]]";
        results = gradingService.extractVariables(text);
        Assert.assertNotNull(results);
        Assert.assertEquals(3, results.size());
        Assert.assertEquals("A", results.get(0));
        Assert.assertEquals("B", results.get(1));
        Assert.assertEquals("C", results.get(2));
        results = gradingService.extractFormulas(text);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("D", results.get(0));
    }

    @Test
    public void testExtractInstructionSegments() {
        List<String> results = null;

        results = gradingService.extractInstructionSegments(sampleItemText1);
        Assert.assertNotNull(results);
        Assert.assertEquals(2, results.size());
        Assert.assertEquals("", results.get(1)); // when formula is at the end it adds an empty string

        results = gradingService.extractInstructionSegments(sampleItemText2);
        Assert.assertNotNull(results);
        Assert.assertEquals(3, results.size());
    }

    @Test
    public void testCalculationsToValues() throws SamigoExpressionError {
        String result = null;

        result = gradingService.replaceCalculationsWithValues("four=[[2+2]]", 0);
        Assert.assertNotNull(result);
        Assert.assertEquals("four=4", result);

        result = gradingService.replaceCalculationsWithValues("four=[[2.2+2]]", 1);
        Assert.assertNotNull(result);
        Assert.assertEquals("four=4.2", result);

        result = gradingService.replaceCalculationsWithValues("four=[[2+2]]", -1);
        Assert.assertNotNull(result);
        Assert.assertEquals("four=4", result);

        result = gradingService.replaceCalculationsWithValues("four=[[2+2]], four=[[2*2]], four=[[(10-2)/2]]", 0);
        Assert.assertNotNull(result);
        Assert.assertEquals("four=4, four=4, four=4", result);

        result = gradingService.replaceCalculationsWithValues("test=[[\n  2 + 10 + 2]]", 0);
        Assert.assertNotNull(result);
        Assert.assertEquals("test=14", result);

        // blanks and line returns in formulas cause problems for the parser
        result = gradingService.processFormulaIntoValue("\n  2 + 10 + 2", 0);
        Assert.assertNotNull(result);
        Assert.assertEquals("14", result);

        result = gradingService.processFormulaIntoValue("\n  2 + 10 + 2", 0);
        Assert.assertNotNull(result);
        Assert.assertEquals("14", result);

        result = gradingService.processFormulaIntoValue("\n  5 \n +  2.0\n +  3  ", 0);
        Assert.assertNotNull(result);
        Assert.assertEquals("10", result);

        // https://jira.sakaiproject.org/browse/SAM-2157
        // 500 formula processor error occurs when either ^ or / is entered in a Formula (at any time) 
        result = gradingService.processFormulaIntoValue("2^2", 0);
        Assert.assertNotNull(result);
        Assert.assertEquals("4", result);
        result = gradingService.processFormulaIntoValue("2^1", 0);
        Assert.assertNotNull(result);
        Assert.assertEquals("2", result);
        result = gradingService.processFormulaIntoValue("2^0", 0);
        Assert.assertNotNull(result);
        Assert.assertEquals("1", result);
        result = gradingService.processFormulaIntoValue("10^1", 0);
        Assert.assertNotNull(result);
        Assert.assertEquals("10", result);
        result = gradingService.processFormulaIntoValue("1^10", 0);
        Assert.assertNotNull(result);
        Assert.assertEquals("1", result);
        result = gradingService.processFormulaIntoValue("2 ^ 2", 0); // with spaces
        Assert.assertNotNull(result);
        Assert.assertEquals("4", result);

        result = gradingService.processFormulaIntoValue("2/2", 0);
        Assert.assertNotNull(result);
        Assert.assertEquals("1", result);
        result = gradingService.processFormulaIntoValue("2/1", 0);
        Assert.assertNotNull(result);
        Assert.assertEquals("2", result);
        result = gradingService.processFormulaIntoValue("0/2", 0);
        Assert.assertNotNull(result);
        Assert.assertEquals("0", result);
        result = gradingService.processFormulaIntoValue("10/2", 0);
        Assert.assertNotNull(result);
        Assert.assertEquals("5", result);
        result = gradingService.processFormulaIntoValue("2/10", 1);
        Assert.assertNotNull(result);
        Assert.assertEquals("0.2", result);
        result = gradingService.processFormulaIntoValue("2 / 2", 0); // with spaces
        Assert.assertNotNull(result);
        Assert.assertEquals("1", result);

        // 500 error occurs when when asin or acos is entered in the formula fields
        result = gradingService.processFormulaIntoValue("sin(0)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("0", result);
        result = gradingService.processFormulaIntoValue("cos(0)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("1", result);
        result = gradingService.processFormulaIntoValue("tan(0)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("0", result);
        result = gradingService.processFormulaIntoValue("sin(PI)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("1.23E-16", result);
        result = gradingService.processFormulaIntoValue("cos(PI)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("-1", result);
        result = gradingService.processFormulaIntoValue("tan(PI)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("-1.23E-16", result);
        result = gradingService.processFormulaIntoValue("sin(1)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("0.84", result);
        result = gradingService.processFormulaIntoValue("cos(1)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("0.54", result);
        result = gradingService.processFormulaIntoValue("tan(1)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("1.56", result);

        result = gradingService.processFormulaIntoValue("asin(0)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("0", result);
        result = gradingService.processFormulaIntoValue("acos(0)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("1.57", result);
        result = gradingService.processFormulaIntoValue("atan(0)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("0", result);
        result = gradingService.processFormulaIntoValue("asin(1)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("1.57", result);
        result = gradingService.processFormulaIntoValue("acos(1)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("0", result);
        result = gradingService.processFormulaIntoValue("atan(1)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("0.79", result);
        
        result = gradingService.processFormulaIntoValue("log(E)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("1", result);
        result = gradingService.processFormulaIntoValue("log(1)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("0", result);
        result = gradingService.processFormulaIntoValue("log(10.0)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("2.3", result); 
        
        result = gradingService.processFormulaIntoValue("exp(1)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("2.72", result);
        result = gradingService.processFormulaIntoValue("exp(5)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("148.41", result);
        result = gradingService.processFormulaIntoValue("exp(10)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("22026.47", result); 
        result = gradingService.processFormulaIntoValue("exp(0)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("1", result);
        
        result = gradingService.processFormulaIntoValue("sqrt(9)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("3", result);
        result = gradingService.processFormulaIntoValue("sqrt(12)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("3.46", result);
        result = gradingService.processFormulaIntoValue("sqrt(25)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("5", result); 
        result = gradingService.processFormulaIntoValue("sqrt(1e-20)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("1E-10", result);   
        
        result = gradingService.processFormulaIntoValue("abs(1)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("1", result);
        result = gradingService.processFormulaIntoValue("abs(-1)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("1", result);
        result = gradingService.processFormulaIntoValue("abs(1e10)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("10000000000", result); 
        result = gradingService.processFormulaIntoValue("abs(-1e10)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("10000000000", result);
        result = gradingService.processFormulaIntoValue("abs(1e-10)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("1E-10", result); 
        result = gradingService.processFormulaIntoValue("abs(-1e-10)", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("1E-10", result);                 
        result = gradingService.processFormulaIntoValue("1000000000.01", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("1000000000.01", result);                 
        result = gradingService.processFormulaIntoValue("10000000000.01", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("10000000000.01", result);                 
        result = gradingService.processFormulaIntoValue("100000000000.01", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("1E11", result);                 
        result = gradingService.processFormulaIntoValue("1000000000000.00", 2);
        Assert.assertNotNull(result);
        Assert.assertEquals("1E12", result);                 
    }

    @Test
    public void testReplaceMappedVariablesWithNumbers() throws Exception {
        String input = null;
        String result = null;
        String expected = null;
        String expected2 = null;
        Map<String, String> map = null;

        input = "this is {var1}";
        map = new HashMap<String, String>() {{
            put("var1", "simple");
        }};
        result = gradingService.replaceMappedVariablesWithNumbers(input, map);
        Assert.assertNotNull(result);
        Assert.assertEquals("this is simple", result);

        input = "this is {var1} + {var2} + {var3} = {{formula1}}";
        map = new HashMap<String, String>() {{
            put("var1", "10");
            put("var2", "5");
            put("var3", "1");
        }};
        result = gradingService.replaceMappedVariablesWithNumbers(input, map);
        Assert.assertNotNull(result);
        Assert.assertEquals("this is 10 + 5 + 1 = {{formula1}}", result);

        input = "this is {var1} + {var2} + {var3} = {{formula1}}, total=[[{var1}+{var2}+{var3}]]";
        expected = "this is 10 + 5 + 1 = {{formula1}}, total=[[10+5+1]]";
        expected2 = "this is 10 + 5 + 1 = {{formula1}}, total=16";
        map = new HashMap<String, String>() {{
            put("var1", "10");
            put("var2", "5");
            put("var3", "1");
        }};
        result = gradingService.replaceMappedVariablesWithNumbers(input, map);
        Assert.assertNotNull(result);
        Assert.assertEquals(expected, result);
        result = gradingService.replaceCalculationsWithValues(expected, 2);
        Assert.assertNotNull(result);
        Assert.assertEquals(expected2, result);

        input = "<p> SAMPLE 1 for testing:<br /> {A} + {B} = {{answer1}}<br /> {B} + {C} = {{answer2}}<br /> {C} + {D} = {{answer3}}<br /> Total: [[{A} +   {B} + {C} + {D}]]</p>";
        expected = "<p> SAMPLE 1 for testing:<br /> 1 + 2 = {{answer1}}<br /> 2 + 3 = {{answer2}}<br /> 3 + 4 = {{answer3}}<br /> Total: [[1 +   2 + 3 + 4]]</p>";
        expected2 = "<p> SAMPLE 1 for testing:<br /> 1 + 2 = {{answer1}}<br /> 2 + 3 = {{answer2}}<br /> 3 + 4 = {{answer3}}<br /> Total: 10</p>";
        map = new HashMap<String, String>() {{
            put("A", "1");
            put("B", "2");
            put("C", "3");
            put("D", "4");
        }};
        result = gradingService.replaceMappedVariablesWithNumbers(input, map);
        Assert.assertNotNull(result);
        Assert.assertEquals(expected, result);
        result = gradingService.replaceCalculationsWithValues(expected, 2);
        Assert.assertNotNull(result);
        Assert.assertEquals(expected2, result);

    }

    @Test
    public void testApplyPrecisionToNumberString() {
        String result;

        result = gradingService.applyPrecisionToNumberString("123", 4);
        Assert.assertNotNull(result);
        Assert.assertEquals("123", result);

        result = gradingService.applyPrecisionToNumberString("1.0", 0);
        Assert.assertNotNull(result);
        Assert.assertEquals("1", result);

        result = gradingService.applyPrecisionToNumberString("0", 1);
        Assert.assertNotNull(result);
        Assert.assertEquals("0", result);

        result = gradingService.applyPrecisionToNumberString("123.000", 0);
        Assert.assertNotNull(result);
        Assert.assertEquals("123", result);

        result = gradingService.applyPrecisionToNumberString("123.000", 4);
        Assert.assertNotNull(result);
        Assert.assertEquals("123", result);

        result = gradingService.applyPrecisionToNumberString("123.0102000", 4);
        Assert.assertNotNull(result);
        Assert.assertEquals("123.0102", result);

        result = gradingService.applyPrecisionToNumberString("123.0102000", 3);
        Assert.assertNotNull(result);
        Assert.assertEquals("123.01", result);
    }
}
