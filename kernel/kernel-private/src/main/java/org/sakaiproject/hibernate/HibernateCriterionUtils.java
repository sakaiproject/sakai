package org.sakaiproject.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public final class HibernateCriterionUtils {
    // ORA-01795: maximum number of expressions in a list is 1000
    public static int MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST = 999;

    private HibernateCriterionUtils() {
        throw new RuntimeException("This class isn't meant to be instantiated");
    }

    public static Criterion CriterionInRestrictionSplitter(String property, Collection<?> values) {
        Objects.requireNonNull(property);
        Objects.requireNonNull(values);

        Criterion criterion = null;
        List<?> list = new ArrayList<>(values);
        int listSize = list.size();

        for (int i = 0; i < listSize; i += MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST) {
            List<?> subList;
            if (listSize > i + MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST) {
                subList = list.subList(i, (i + MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST));
            } else {
                subList = list.subList(i, listSize);
            }
            if (criterion == null) {
                criterion = Restrictions.in(property, subList);
            } else {
                criterion = Restrictions.or(criterion, Restrictions.in(property, subList));
            }
        }
        return criterion;
    }
}
