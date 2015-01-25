package org.sakaiproject.gradebookng.tool.panels;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.gradebookng.business.model.ImportedGrade;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chmaurer on 1/22/15.
 */
public class GradeImportConfirmationStep extends Panel {

    private static final Logger log = Logger.getLogger(GradeImportConfirmationStep.class);

    public GradeImportConfirmationStep(String id, List<ImportedGrade> grades) {
        super(id);

        final CheckGroup<ImportedGrade> group = new CheckGroup<ImportedGrade>("group", new ArrayList<ImportedGrade>());

        Form<?> form = new Form("form")
        {
            @Override
            protected void onSubmit()
            {
                info("selected grade(s): " + group.getDefaultModelObjectAsString());
            }
        };
        add(form);
        form.add(group);
        group.add(new CheckGroupSelector("groupselector"));
        ListView<ImportedGrade> gradeList = new ListView<ImportedGrade>("grades",
                grades)
        {
            /**
             * @see org.apache.wicket.markup.html.list.ListView#populateItem(org.apache.wicket.markup.html.list.ListItem)
             */
            @Override
            protected void populateItem(ListItem<ImportedGrade> item)
            {
                item.add(new Check<ImportedGrade>("checkbox", item.getModel()));
                item.add(new Label("studentId",
                        new PropertyModel<String>(item.getDefaultModel(), "studentId")));
                item.add(new Label("studentName", new PropertyModel<String>(item.getDefaultModel(),
                        "studentName")));
            }

        };

        gradeList.setReuseItems(true);
        group.add(gradeList);

    }

}
