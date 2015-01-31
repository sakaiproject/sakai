package org.sakaiproject.gradebookng.business.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by chmaurer on 1/27/15.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportColumn implements Serializable {

    public static final int TYPE_REGULAR = 0;
    public static final int TYPE_ITEM_WITH_POINTS = 1;
    public static final int TYPE_ITEM_WITH_COMMENTS = 2;

    private String columnTitle;
    private String points;
    private int type = TYPE_REGULAR;

//    public ImportColumn(String columnTitle, String points, int type) {
//
//    }

}
