/*
 * Copyright (c) 2004 Stephan J. Schmidt All Rights Reserved. --LICENSE NOTICE--
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License. --LICENSE
 * NOTICE--
 */
/*
 * package org.radeox.example; import groovy.text.SimpleTemplateEngine; import
 * groovy.text.Template; import groovy.text.TemplateEngine; import
 * org.codehaus.groovy.syntax.SyntaxException; import
 * org.radeox.api.engine.RenderEngine; import
 * org.radeox.api.engine.context.RenderContext; import
 * org.radeox.engine.BaseRenderEngine; import
 * org.radeox.engine.context.BaseRenderContext; import java.io.IOException;
 * import java.io.Reader; import java.io.StringReader; /** Groovy Template
 * Engine which uses Radeox to render text markup @author Stephan J. Schmidt
 * 
 * @version $Id: RadeoxTemplateEngine.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $ / public class RadeoxTemplateEngine extends
 *          TemplateEngine { public Template createTemplate(Reader reader)
 *          throws SyntaxException, ClassNotFoundException, IOException {
 *          RenderContext context = new BaseRenderContext(); RenderEngine engine =
 *          new BaseRenderEngine(); String renderedText = engine.render(reader ,
 *          context); TemplateEngine templateEngine = new
 *          SimpleTemplateEngine(); return templateEngine.createTemplate(new
 *          StringReader(renderedText)); } }
 */