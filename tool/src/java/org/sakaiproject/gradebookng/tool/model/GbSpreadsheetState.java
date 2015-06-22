package org.sakaiproject.gradebookng.tool.model;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

public class GbSpreadsheetState implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter @Setter
    private boolean categoriesEnabled;
    
    public GbSpreadsheetState() {
        this.categoriesEnabled = false;
    }
}
