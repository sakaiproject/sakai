/*
 * This file is part of "SnipSnap Radeox Rendering Engine". Copyright (c) 2002
 * Stephan J. Schmidt, Matthias L. Jugel All Rights Reserved. Please visit
 * http://radeox.org/ for updates and contact. --LICENSE NOTICE-- Licensed under
 * the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. --LICENSE NOTICE--
 */

/*
 * package org.radeox.test; import com.clarkware.junitperf.TimedTest; import
 * junit.framework.Test; import junit.framework.TestSuite; import
 * junit.textui.TestRunner; import org.radeox.EngineManager; import
 * org.radeox.engine.context.BaseRenderContext; import
 * org.radeox.util.logging.Logger; import org.radeox.util.logging.NullLogger;
 * import java.io.BufferedReader; import java.io.File; import
 * java.io.FileReader; import java.io.IOException; public class PerformanceTests {
 * public static void main(String[] args) throws IOException {
 * TestRunner.run(suite()); } public static Test suite() throws IOException { //
 * get test markup from text file File wikiTxt = new File("wiki.txt");
 * BufferedReader reader = new BufferedReader(new
 * FileReader(wikiTxt.getCanonicalFile())); StringBuffer input = new
 * StringBuffer(); String tmp; while ((tmp = reader.readLine()) != null) {
 * input.append(tmp); } Logger.setHandler(new NullLogger());
 * System.err.println(EngineManager.getInstance().render("__initialized__", new
 * BaseRenderContext())); TestSuite s = new TestSuite(); long maxElapsedTime =
 * 30 * 1000; // 30s StringBuffer testString = new StringBuffer(); for (int i =
 * 0; i < 10; i++) { testString.append(input); Test renderEngineTest = new
 * RenderEnginePerformanceTest(testString.toString()); Test timedTest = new
 * TimedTest(renderEngineTest, maxElapsedTime, false); s.addTest(timedTest); }
 * return s; } }
 */