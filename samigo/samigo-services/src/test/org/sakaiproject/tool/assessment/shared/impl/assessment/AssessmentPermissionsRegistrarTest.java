package org.sakaiproject.tool.assessment.shared.impl.assessment;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.samigo.util.SamigoConstants;

@RunWith(MockitoJUnitRunner.class)
public class AssessmentPermissionsRegistrarTest {

    @Mock
    private FunctionManager functionManager;

    private AssessmentPermissionsRegistrar registrar;

    @Before
    public void setUp() {
        registrar = new AssessmentPermissionsRegistrar();
        registrar.setFunctionManager(functionManager);
    }

    @Test
    public void initRegistersAssessmentFunctionsAsUserMutable() {
        registrar.init();

        verify(functionManager).registerFunction(SamigoConstants.AUTHZ_TAKE_ASSESSMENT, true);
        verify(functionManager).registerFunction(SamigoConstants.AUTHZ_SUBMIT_ASSESSMENT, true);
        verify(functionManager).registerFunction(SamigoConstants.AUTHZ_CREATE_ASSESSMENT, true);
        verify(functionManager).registerFunction(SamigoConstants.AUTHZ_EDIT_ASSESSMENT_ANY, true);
        verify(functionManager).registerFunction(SamigoConstants.AUTHZ_EDIT_ASSESSMENT_OWN, true);
        verify(functionManager).registerFunction(SamigoConstants.AUTHZ_DELETE_ASSESSMENT_ANY, true);
        verify(functionManager).registerFunction(SamigoConstants.AUTHZ_DELETE_ASSESSMENT_OWN, true);
        verify(functionManager).registerFunction(SamigoConstants.AUTHZ_PUBLISH_ASSESSMENT_ANY, true);
        verify(functionManager).registerFunction(SamigoConstants.AUTHZ_PUBLISH_ASSESSMENT_OWN, true);
        verify(functionManager).registerFunction(SamigoConstants.AUTHZ_GRADE_ASSESSMENT_ANY, true);
        verify(functionManager).registerFunction(SamigoConstants.AUTHZ_GRADE_ASSESSMENT_OWN, true);
        verify(functionManager).registerFunction(SamigoConstants.AUTHZ_QUESTIONPOOL_CREATE, true);
        verify(functionManager).registerFunction(SamigoConstants.AUTHZ_QUESTIONPOOL_EDIT_OWN, true);
        verify(functionManager).registerFunction(SamigoConstants.AUTHZ_QUESTIONPOOL_DELETE_OWN, true);
        verify(functionManager).registerFunction(SamigoConstants.AUTHZ_QUESTIONPOOL_COPY_OWN, true);
        verify(functionManager).registerFunction(SamigoConstants.AUTHZ_TEMPLATE_CREATE, true);
        verify(functionManager).registerFunction(SamigoConstants.AUTHZ_TEMPLATE_EDIT_OWN, true);
        verify(functionManager).registerFunction(SamigoConstants.AUTHZ_TEMPLATE_DELETE_OWN, true);
        verify(functionManager).registerFunction(SamigoConstants.AUTHZ_ASSESSMENT_ALL_GROUPS, true);
        verifyNoMoreInteractions(functionManager);
    }
}
