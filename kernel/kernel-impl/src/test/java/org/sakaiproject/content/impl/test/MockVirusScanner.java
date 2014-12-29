/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl.test;

import org.mockito.Mockito;
import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.antivirus.api.VirusScanner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 9/16/13
 * Time: 1:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class MockVirusScanner {

    static public VirusScanner virusScannerFound() {
        VirusScanner virusScanner = Mockito.mock(VirusScanner.class);
        when(virusScanner.getEnabled()).thenReturn(true);
        doThrow(new VirusFoundException("virus found")).when(virusScanner).scanContent(anyString());
        return virusScanner;
    }

    static public VirusScanner virusScannerNotFound() {
        VirusScanner virusScanner = Mockito.mock(VirusScanner.class);
        doNothing().when(virusScanner).scanContent(anyString());
        return virusScanner;
    }
}
