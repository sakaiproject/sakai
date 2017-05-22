/*
 * This file is part of "Radeox". Copyright (c) 2003 Stephan J. Schmidt All
 * Rights Reserved. --LICENSE NOTICE-- Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. --LICENSE NOTICE--
 */

/*
 * package org.radeox.test.groovy; import groovy.text.Template; import
 * groovy.text.TemplateEngine; import junit.framework.TestCase; import
 * java.util.HashMap; import java.util.Map; import
 * org.radeox.example.RadeoxTemplateEngine; public class
 * RadeoxTemplateEngineTest extends TestCase { public
 * RadeoxTemplateEngineTest(String name) { super(name); } public void
 * testRadeoxTemplate() { String text = "__Dear__ ${firstname}"; Map binding =
 * new HashMap(); binding.put("firstname", "stephan"); TemplateEngine engine =
 * new RadeoxTemplateEngine(); Template template = null; try { template =
 * engine.createTemplate(text); } catch (Exception e) { e.printStackTrace(); }
 * template.setBinding(binding); String result = "<b class=\"bold\">Dear</b>
 * stephan"; assertEquals(result, template.toString()); } }
 */