package org.sakaiproject.gradebookng.tool.model;

import org.apache.wicket.model.LoadableDetachableModel;

/**
 * Created by chmaurer on 3/3/15.
 */
public class GbAssignmentModel<GbAssignment> extends LoadableDetachableModel<GbAssignment> {

    private static final long serialVersionUID = 1L;

    private GbAssignment gbAssignment;

    public GbAssignmentModel(GbAssignment gbAssignment) {
        this.gbAssignment = gbAssignment;
    }

    @Override
    protected GbAssignment load() {
        return gbAssignment;
    }
}
