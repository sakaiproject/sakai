/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
