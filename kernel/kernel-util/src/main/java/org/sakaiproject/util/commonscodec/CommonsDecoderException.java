/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
/*
 * This has been added to avoid needing to put commons codec into shared 
 */
package org.sakaiproject.util.commonscodec;

/**
 * Thrown when a Decoder has encountered a failure condition during a decode. 
 * 
 * @author Apache Software Foundation
 * @version SourceVersion: DecoderException.java,v 1.9 2004/02/29 04:08:31 tobrien Exp
 * 
 */
public class CommonsDecoderException extends Exception {

    /**
     * Creates a DecoderException
     * 
     * @param pMessage A message with meaning to a human
     */
    public CommonsDecoderException(String pMessage) {
        super(pMessage);
    }

} 