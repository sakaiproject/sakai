package org.sakaiproject.msgcntr.tool;

import org.junit.Test;
import org.sakaiproject.tool.messageforums.util.PrivateMessagesToolHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class PrivateMessagesToolHelperTest {

    @Test
    public void testRemoveRecipientUndisclosed() {
        // Test case 1: Remove undisclosed recipients
        String expectedResult1 = "Alan, Nat (322)";
        String result1 = PrivateMessagesToolHelper.removeRecipientUndisclosed(
                "Alan, Nat (322); Undisclosed-Recipients ; ",
                "Undisclosed-Recipients"
        );
        assertEquals(expectedResult1, result1);

        // Test case 2: Remove undisclosed recipients and semicolon
        String expectedResult2 = "Alan, Nat (322)";
        String result2 = PrivateMessagesToolHelper.removeRecipientUndisclosed(
                "Alan, Nat (322); Undisclosed-Recipients; ",
                "Undisclosed-Recipients"
        );
        assertEquals(expectedResult2, result2);

        // Test case 3: recipientsAsText is null
        String recipientsAsText3 = null;
        String recipientsUndisclosed3 = "Undisclosed-Recipients";
        assertThrows(IllegalArgumentException.class, () -> PrivateMessagesToolHelper.removeRecipientUndisclosed(recipientsAsText3, recipientsUndisclosed3));

        // Test case 4: recipientsUndisclosed is null
        String recipientsAsText4 = "Alan, Nat (322); Undisclosed-Recipients ; ";
        String recipientsUndisclosed4 = null;
        assertThrows(IllegalArgumentException.class, () -> PrivateMessagesToolHelper.removeRecipientUndisclosed(recipientsAsText4, recipientsUndisclosed4));

        // Test case 5: Remove semicolon
        String expectedResult5 = "Alan, Nat (322)";
        String result5 = PrivateMessagesToolHelper.removeRecipientUndisclosed(
                "Alan, Nat (322); ",
                "Undisclosed-Recipients"
        );
        assertEquals(expectedResult5, result5);
    }
}
