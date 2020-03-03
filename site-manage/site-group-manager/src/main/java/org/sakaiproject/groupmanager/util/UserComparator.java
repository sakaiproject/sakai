package org.sakaiproject.groupmanager.util;

import java.text.Collator;
import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.sakaiproject.user.api.User;

public class UserComparator implements Comparator<User> {

    private final Collator collator = Collator.getInstance();

    @Override
    public int compare(final User lhs, final User rhs) {
        this.collator.setStrength(Collator.PRIMARY);
        return new CompareToBuilder().append(lhs.getDisplayName(), rhs.getDisplayName(), this.collator).toComparison();
    }

}
