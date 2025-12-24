package org.sakaiproject.announcement.tool;

import org.mockito.Mockito;
import org.sakaiproject.lti.api.LTIService;

public class MockFactory {
    public LTIService ltiService() {
        return Mockito.mock(LTIService.class);
    }
}