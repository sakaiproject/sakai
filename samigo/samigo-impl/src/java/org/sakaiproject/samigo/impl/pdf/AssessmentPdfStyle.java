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
package org.sakaiproject.samigo.impl.pdf;

import java.awt.Color;

import com.lowagie.text.Font;
import com.lowagie.text.PageSize;

/**
 * Shared PDF colour and font constants for Samigo assessment documents.
 */
public final class AssessmentPdfStyle {

    public static final Color PRIMARY_COLOR = new Color(60, 64, 67);
    public static final Color SECONDARY_COLOR = new Color(95, 99, 104);
    public static final Color BACKGROUND_GRAY = new Color(243, 244, 246);
    public static final Color BORDER_COLOR = new Color(229, 231, 235);
    public static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    public static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    public static final Color TEXT_LINK = new Color(37, 99, 235);
    public static final Color SUCCESS_COLOR = new Color(16, 185, 129);
    public static final Color SUCCESS_BG = new Color(220, 252, 231);
    public static final Color WARNING_COLOR = new Color(245, 158, 11);
    public static final Color WARNING_BG = new Color(254, 243, 199);
    public static final Color ERROR_COLOR = new Color(239, 68, 68);
    public static final Color INFO_BG = new Color(219, 234, 254);
    public static final Color FEEDBACK_BG = new Color(233, 213, 255);
    public static final Color WHITE_COLOR = new Color(255, 255, 255);

    public static final Font TITLE_FONT = new Font(Font.HELVETICA, 20, Font.BOLD);
    public static final Font HEADING_FONT = new Font(Font.HELVETICA, 14, Font.BOLD);
    public static final Font HEADING_NORMAL_FONT = new Font(Font.HELVETICA, 14, Font.NORMAL);
    public static final Font BODY_BOLD_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);
    public static final Font BODY_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);
    public static final Font BODY_ITALIC_FONT = new Font(Font.HELVETICA, 10, Font.ITALIC);
    public static final Font SMALL_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL);
    public static final Font SMALL_BOLD_FONT = new Font(Font.HELVETICA, 8, Font.BOLD);

    public static final float MAX_IMAGE_HEIGHT = PageSize.A4.getHeight() * 0.16f;

    public static final float ELEMENT_SPACING = 12f;

    private AssessmentPdfStyle() {
    }

    public static Font fontWithColor(Font baseFont, Color color) {
        return new Font(baseFont.getFamily(), baseFont.getSize(), baseFont.getStyle(), color);
    }

    /**
     * Scales body/small fonts for printable assessment documents.
     *
     * @param fontSizeSetting PrintSettingsBean font size 1–5
     */
    public static float fontScale(String fontSizeSetting) {
        if (fontSizeSetting == null) {
            return 1f;
        }
        switch (fontSizeSetting) {
            case "1": return 0.65f;
            case "2": return 0.8f;
            case "4": return 1.3f;
            case "5": return 1.6f;
            default: return 1f;
        }
    }

    public static Font scaledFont(Font base, String fontSizeSetting) {
        float scale = fontScale(fontSizeSetting);
        return new Font(base.getFamily(), Math.round(base.getSize() * scale), base.getStyle(), base.getColor());
    }
}
