package org.sakaiproject.assignment.impl;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by enietzel on 5/19/17.
 */
@Slf4j
public class AssignmentIdentityTest {

    @Test
    public void assignmentEqualsIdentity() {
        Assignment a1 = new Assignment();
        Assignment a2 = new Assignment();
        Assert.assertTrue(a1.equals(a2));

        a1.setId(UUID.randomUUID().toString());
        Assert.assertFalse(a1.equals(a2));

        a2.setId(a1.getId());
        Assert.assertTrue(a1.equals(a2));

        a1.setContext(UUID.randomUUID().toString());
        Assert.assertTrue(a1.equals(a2));
    }

    @Test
    public void assignmentHashcodeIdentity() {
        Assignment a = new Assignment();
        Assert.assertEquals(a.hashCode(), a.hashCode());

        String id = UUID.randomUUID().toString();
        a.setId(id);
        Assert.assertEquals(hashCode(id), a.hashCode());
    }

    @Test
    public void assignmentSubmissionEqualsIdentity() {
        AssignmentSubmission s1 = new AssignmentSubmission();
        AssignmentSubmission s2 = new AssignmentSubmission();
        Assert.assertEquals(s1.hashCode(), s2.hashCode());

        s1.setId(UUID.randomUUID().toString());
        Assert.assertNotEquals(s1.hashCode(), s2.hashCode());

        s2.setId(s1.getId());
        Assert.assertEquals(s1.hashCode(), s2.hashCode());

        s2.setId(UUID.randomUUID().toString());

        Set<AssignmentSubmission> submissions = new HashSet<>();
        submissions.add(s1);
        submissions.add(s2);

        Assert.assertEquals(2, submissions.size());
        Assert.assertTrue(submissions.contains(s1));
        Assert.assertTrue(submissions.contains(s2));

        submissions.remove(s1);
        Assert.assertEquals(1, submissions.size());

        submissions.remove(s2);
        Assert.assertEquals(0, submissions.size());
    }

    public int hashCode(final String id) {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (id == null ? 43 : id.hashCode());
        return result;
    }
}
