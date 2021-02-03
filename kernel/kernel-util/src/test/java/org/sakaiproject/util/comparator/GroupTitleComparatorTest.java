package org.sakaiproject.util.comparator;

import org.junit.Test;
import org.sakaiproject.site.api.Group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GroupTitleComparatorTest {

    @Test
    public void getSortedGroupsWithAlpha() {
        final GroupTitleComparator groupComparator = new GroupTitleComparator();

        Group groupA = mock(Group.class);
        when(groupA.getTitle()).thenReturn("Group 1");

        Group groupB = mock(Group.class);
        when(groupB.getTitle()).thenReturn("Group 2");

        Group groupC = mock(Group.class);
        when(groupC.getTitle()).thenReturn("Group 11");

        Group groupD = mock(Group.class);
        when(groupD.getTitle()).thenReturn("Group 112");

        List<Group> groupList = new ArrayList<>();
        groupList.add(groupA);
        groupList.add(groupB);
        groupList.add(groupC);
        groupList.add(groupD);

        Collections.sort(groupList, groupComparator);

        assertEquals(groupList.get(0).getTitle(), "Group 1");
        assertEquals(groupList.get(1).getTitle(), "Group 2");
        assertEquals(groupList.get(2).getTitle(), "Group 11");
        assertEquals(groupList.get(3).getTitle(), "Group 112");
    }

}