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
