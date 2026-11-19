/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.ui.bean.print.settings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class PrintSettingsBeanTest {

    @Test
    public void setShowKeysFalseClearsShowKeysFeedback() {
        PrintSettingsBean settings = new PrintSettingsBean();
        settings.setShowKeys(Boolean.TRUE);
        settings.setShowKeysFeedback(Boolean.TRUE);

        settings.setShowKeys(Boolean.FALSE);

        assertFalse(settings.getShowKeys());
        assertFalse(settings.getShowKeysFeedback());
    }

    @Test
    public void setShowKeysTrueLeavesShowKeysFeedbackUnchanged() {
        PrintSettingsBean settings = new PrintSettingsBean();
        settings.setShowKeysFeedback(Boolean.TRUE);

        settings.setShowKeys(Boolean.TRUE);

        assertTrue(settings.getShowKeys());
        assertTrue(settings.getShowKeysFeedback());
    }
}
