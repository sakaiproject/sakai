/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.export;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.attendance.export.util.SortNameUserComparator;
import org.sakaiproject.attendance.logic.AttendanceLogic;
import org.sakaiproject.attendance.logic.SakaiProxy;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.user.api.User;

import java.awt.Color;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Implementation of PDFEventExporter, {@link org.sakaiproject.attendance.export.PDFEventExporter}
 *
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
@Slf4j
public class PDFEventExporterImpl implements PDFEventExporter {

    private static final Font h1 = new Font(Font.HELVETICA, 18, Font.BOLD, Color.BLACK);
    private static final Font h3 = new Font(Font.HELVETICA, 14, Font.BOLD, Color.BLACK);
    private static final Font body = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);

    private static final Font tableHeader = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);

    private AttendanceEvent event;
    private Document document;
    private List<User> users;
    private String groupOrSiteTitle;

    /**
     * {@inheritDoc}
     */
    public void createSignInPdf(AttendanceEvent event, OutputStream outputStream, List<User> usersToPrint, String groupOrSiteTitle) {

        this.event = event;
        this.document = new Document();
        this.users = usersToPrint;
        this.groupOrSiteTitle = groupOrSiteTitle;

        buildDocumentShell(outputStream, true);
    }

    /**
     * {@inheritDoc}
     */
    public void createAttendanceSheetPdf(AttendanceEvent event, OutputStream outputStream, List<User> usersToPrint, String groupOrSiteTitle) {

        this.event = event;
        this.document = new Document();
        this.users = usersToPrint;
        this.groupOrSiteTitle = groupOrSiteTitle;

        buildDocumentShell(outputStream, false);
    }

    private void buildDocumentShell(OutputStream outputStream, boolean isSignInSheet) {
        String eventName = event.getName();
        Date eventDate = event.getStartDateTime();


        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy h:mm a");

        try {
            PdfWriter.getInstance(document, outputStream);

            document.open();

            String pageTitle = isSignInSheet?"Sign-In Sheet":"Attendance Sheet";

            Paragraph title = new Paragraph(pageTitle + " - " + groupOrSiteTitle, h1);


            document.add(title);

            String eventDateString = eventDate==null?"":" (" + dateFormat.format(eventDate) + ")";

            Paragraph eventHeader = new Paragraph(eventName + eventDateString, h3);
            eventHeader.setSpacingBefore(14);
            document.add(eventHeader);

            if(isSignInSheet) {
                document.add(signInSheetTable());
            } else {
                document.add(attendanceSheetTable());
            }

            document.close(); // no need to close PDFwriter?

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private PdfPTable signInSheetTable() {

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(12);

        PdfPCell nameHeader = new PdfPCell(new Paragraph("Student Name", tableHeader));
        nameHeader.setPadding(10);

        PdfPCell signatureHeader = new PdfPCell(new Paragraph("Signature", tableHeader));
        signatureHeader.setPadding(10);

        table.addCell(nameHeader);
        table.addCell(signatureHeader);

        Collections.sort(users, new SortNameUserComparator());

        for(User user : users) {

            PdfPCell userCell = new PdfPCell(new Paragraph(user.getSortName(), body));
            userCell.setPadding(10);

            PdfPCell blankCell = new PdfPCell(new Paragraph());
            blankCell.setPadding(10);

            table.addCell(userCell);
            table.addCell(blankCell);
        }

        return table;

    }

    private PdfPTable attendanceSheetTable() {

        List<AttendanceStatus> activeStatuses = attendanceLogic.getActiveStatusesForSite(event.getAttendanceSite());
        int colSpan = activeStatuses.size() - 1;

        if(colSpan <= 0) {
            colSpan = 1;
        }

        PdfPTable table = new PdfPTable(colSpan * 2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(12);

        PdfPCell nameHeader = new PdfPCell(new Paragraph("Student Name", tableHeader));
        nameHeader.setPadding(10);
        nameHeader.setColspan(colSpan);
        table.addCell(nameHeader);

        int numStatusHeaders = 0;
        for(AttendanceStatus status : activeStatuses) {
            if(status.getStatus() != Status.UNKNOWN) {
                Paragraph statusHeaderParagraph = new Paragraph(getStatusString(status.getStatus(), colSpan), tableHeader);
                statusHeaderParagraph.setAlignment(Element.ALIGN_CENTER);
                PdfPCell statusHeader = new PdfPCell(statusHeaderParagraph);
                statusHeader.setPadding(10);
                table.addCell(statusHeader);
                numStatusHeaders++;
            }
        }
        if(numStatusHeaders == 0) {
            Paragraph statusHeaderParagraph = new Paragraph("Status", tableHeader);
            statusHeaderParagraph.setAlignment(Element.ALIGN_CENTER);
            PdfPCell statusHeader = new PdfPCell(statusHeaderParagraph);
            statusHeader.setPadding(10);
            table.addCell(statusHeader);
        }

        Collections.sort(users, new SortNameUserComparator());

        for(User user : users) {

            PdfPCell userCell = new PdfPCell(new Paragraph(user.getSortName() + " (" + user.getDisplayId() + ")", body));
            userCell.setPadding(10);
            userCell.setColspan(colSpan);

            table.addCell(userCell);

            for(int i=0; i < colSpan; i++) {
                // Add blank cell
                table.addCell(new PdfPCell(new Paragraph()));
            }

        }

        return table;
    }

    /**
     * init - perform any actions required here for when this bean starts up
     */
    public void init() {
        log.debug("PDFEventExporterImpl init()");
    }

    // TODO: Internationalize status header abbreviations
    private String getStatusString(Status s, int numStatuses) {
        if(numStatuses < 4) {
            switch (s)
            {
                case UNKNOWN: return "None";
                case PRESENT: return "Present";
                case EXCUSED_ABSENCE: return "Excused";
                case UNEXCUSED_ABSENCE: return "Absent";
                case LATE: return "Late";
                case LEFT_EARLY: return "Left Early";
                default: return "None";
            }
        } else {
            switch (s)
            {
                case UNKNOWN: return "None";
                case PRESENT: return "Pres";
                case EXCUSED_ABSENCE: return "Excu";
                case UNEXCUSED_ABSENCE: return "Abse";
                case LATE: return "Late";
                case LEFT_EARLY: return "Left";
                default: return "None";
            }
        }
    }

    @Setter
    private SakaiProxy sakaiProxy;

    @Setter
    private AttendanceLogic attendanceLogic;

}
