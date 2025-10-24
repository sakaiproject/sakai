package org.sakaiproject.accountvalidator.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.accountvalidator.api.model.ValidationAccount;
import org.sakaiproject.accountvalidator.api.repository.ValidationAccountRepository;
import org.sakaiproject.accountvalidator.api.service.AccountValidationService;
import org.sakaiproject.accountvalidator.impl.service.AccountValidationServiceImpl;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.AopTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AccountValidationTestConfiguration.class})
public class AccountValidationServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired private AccountValidationService service;
    @Autowired private ValidationAccountRepository repository;
    @Autowired private IdManager idManager;
    @Autowired private ResourceLoader resourceLoader;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private UserDirectoryService userDirectoryService;

    private final String userOne = "user1";
    private final String userTwo = "user2";
    private final String userThree = "user3";

    @Before
    public void setup() throws UserNotDefinedException {
        // Clear any existing test data
        repository.deleteAll();


        Mockito.when(resourceLoader.getLocale()).thenReturn(new java.util.Locale("en", "US"));
        ((AccountValidationServiceImpl) AopTestUtils.getTargetObject(service)).setResourceLoader(resourceLoader);

        // Setup server configuration mocks
        Mockito.when(serverConfigurationService.getServerUrl()).thenReturn("http://localhost:8080");
        Mockito.when(serverConfigurationService.getString("mail.support")).thenReturn("support@example.com");
        Mockito.when(serverConfigurationService.getString("ui.institution")).thenReturn("Test Institution");
        Mockito.when(serverConfigurationService.getSmtpFrom()).thenReturn("noreply@example.com");
        Mockito.when(serverConfigurationService.getString(Mockito.eq("mail.support.name"), Mockito.anyString())).thenReturn("Support");
        Mockito.when(serverConfigurationService.getString(Mockito.anyString(), Mockito.anyString())).thenReturn("Support");
        Mockito.when(serverConfigurationService.getInt(Mockito.anyString(), Mockito.anyInt())).thenReturn(60);

        Mockito.when(idManager.createUuid()).thenReturn(UUID.randomUUID().toString());

        // Pre-create mock users for known test user IDs
        User user1 = createMockUser(userOne);
        User user2 = createMockUser(userTwo);
        User user3 = createMockUser(userThree);
        User nonExistentUser = createMockUser("nonExistentUser");

        // Configure UserDirectoryService to return the appropriate mock user
        Mockito.when(userDirectoryService.getUser(userOne)).thenReturn(user1);
        Mockito.when(userDirectoryService.getUser(userTwo)).thenReturn(user2);
        Mockito.when(userDirectoryService.getUser(userThree)).thenReturn(user3);
        Mockito.when(userDirectoryService.getUser("nonExistentUser")).thenReturn(nonExistentUser);

        Mockito.when(userDirectoryService.userReference(Mockito.anyString())).thenAnswer(invocation ->
            "/user/" + invocation.getArgument(0)
        );
    }

    private User createMockUser(String userId) {
        User mockUser = Mockito.mock(User.class);
        Mockito.when(mockUser.getId()).thenReturn(userId);
        Mockito.when(mockUser.getEid()).thenReturn(userId);
        Mockito.when(mockUser.getDisplayName()).thenReturn("User " + userId);
        Mockito.when(mockUser.getFirstName()).thenReturn("First");
        Mockito.when(mockUser.getLastName()).thenReturn("Last");
        Mockito.when(mockUser.getEmail()).thenReturn(userId + "@example.com");
        Mockito.when(mockUser.getCreatedBy()).thenReturn(mockUser);
        return mockUser;
    }

    @Test
    public void accountValidationServiceIsValid() {
        Assert.assertNotNull("AccountValidationService is null", service);
    }

    @Test
    public void testCreateValidationAccount() {
        ValidationAccount account = service.createValidationAccount(userOne);

        Assert.assertNotNull("Created account should not be null", account);
        Assert.assertNotNull("Account ID should be set", account.getId());
        Assert.assertEquals("User ID should match", userOne, account.getUserId());
        Assert.assertNotNull("Validation token should be set", account.getValidationToken());
        Assert.assertNotNull("Validation sent date should be set", account.getValidationSent());
        Assert.assertEquals("Status should be SENT", ValidationAccount.STATUS_SENT, account.getStatus());
        Assert.assertEquals("Validations sent count should be 1", Integer.valueOf(1), account.getValidationsSent());
        Assert.assertEquals("Account status should be EXISTING",
                Integer.valueOf(ValidationAccount.ACCOUNT_STATUS_EXISTING), account.getAccountStatus());
    }

    @Test
    public void testCreateValidationAccountForNewUser() {
        ValidationAccount account = service.createValidationAccount(userOne, true);

        Assert.assertNotNull("Created account should not be null", account);
        Assert.assertEquals("Account status should be NEW",
                Integer.valueOf(ValidationAccount.ACCOUNT_STATUS_NEW), account.getAccountStatus());
    }

    @Test
    public void testCreateValidationAccountWithStatus() {
        ValidationAccount account = service.createValidationAccount(userOne,
                                                                    ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET);

        Assert.assertNotNull("Created account should not be null", account);
        Assert.assertEquals("Account status should be PASSWORD_RESET",
                Integer.valueOf(ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET), account.getAccountStatus());
    }

    @Test
    public void testCreateValidationAccountForUserIdUpdate() {
        String newUserId = "newUserId123";
        ValidationAccount account = service.createValidationAccount(userOne, newUserId);

        Assert.assertNotNull("Created account should not be null", account);
        Assert.assertEquals("Account status should be USERID_UPDATE",
                Integer.valueOf(ValidationAccount.ACCOUNT_STATUS_USERID_UPDATE), account.getAccountStatus());
        Assert.assertEquals("EID should be set to new user ID", newUserId, account.getEid());
    }

    @Test
    public void testGetValidationAccountById() {
        ValidationAccount created = service.createValidationAccount(userOne);

        ValidationAccount retrieved = service.getValidationAccountById(created.getId());

        Assert.assertNotNull("Retrieved account should not be null", retrieved);
        Assert.assertEquals("Account IDs should match", created.getId(), retrieved.getId());
        Assert.assertEquals("User IDs should match", userOne, retrieved.getUserId());
    }

    @Test
    public void testGetValidationAccountByIdNotFound() {
        ValidationAccount retrieved = service.getValidationAccountById(999999L);

        Assert.assertNull("Retrieved account should be null for non-existent ID", retrieved);
    }

    @Test
    public void testGetValidationAccountByToken() {
        ValidationAccount created = service.createValidationAccount(userOne);

        ValidationAccount retrieved = service.getValidationAccountBytoken(created.getValidationToken());

        Assert.assertNotNull("Retrieved account should not be null", retrieved);
        Assert.assertEquals("Validation tokens should match", created.getValidationToken(), retrieved.getValidationToken());
        Assert.assertEquals("User IDs should match", userOne, retrieved.getUserId());
    }

    @Test
    public void testGetValidationAccountByTokenNotFound() {
        ValidationAccount retrieved = service.getValidationAccountBytoken("non-existent-token");

        Assert.assertNull("Retrieved account should be null for non-existent token", retrieved);
    }

    @Test
    public void testGetValidationAccountByUserId() {
        service.createValidationAccount(userOne);

        ValidationAccount retrieved = service.getValidationAccountByUserId(userOne);

        Assert.assertNotNull("Retrieved account should not be null", retrieved);
        Assert.assertEquals("User IDs should match", userOne, retrieved.getUserId());
    }

    @Test
    public void testGetValidationAccountByUserIdNotFound() {
        ValidationAccount retrieved = service.getValidationAccountByUserId("nonExistentUser");

        Assert.assertNull("Retrieved account should be null for non-existent user", retrieved);
    }

    @Test
    public void testSaveValidationAccount() {
        ValidationAccount account = new ValidationAccount();
        account.setUserId(userOne);
        account.setValidationToken("save-test-token");
        account.setStatus(ValidationAccount.STATUS_SENT);
        account.setValidationSent(Instant.now());
        account.setValidationsSent(1);
        account.setAccountStatus(ValidationAccount.ACCOUNT_STATUS_NEW);
        account.setFirstName("Test");
        account.setSurname("User");

        service.save(account);

        Assert.assertNotNull("Account ID should be set after save", account.getId());

        ValidationAccount retrieved = service.getValidationAccountById(account.getId());
        Assert.assertNotNull("Saved account should be retrievable", retrieved);
        Assert.assertEquals("User IDs should match", userOne, retrieved.getUserId());
    }

    @Test
    public void testSaveHandlesEmptyStrings() {
        ValidationAccount account = new ValidationAccount();
        account.setUserId(userOne);
        account.setValidationToken("empty-test-token");
        account.setStatus(ValidationAccount.STATUS_SENT);
        account.setValidationSent(Instant.now());
        account.setValidationsSent(1);
        account.setAccountStatus(ValidationAccount.ACCOUNT_STATUS_NEW);
        account.setFirstName("");
        account.setSurname("");

        service.save(account);

        ValidationAccount retrieved = service.getValidationAccountById(account.getId());
        Assert.assertNull("Empty first name should be converted to null", retrieved.getFirstName());
        Assert.assertNull("Empty surname should be converted to null", retrieved.getSurname());
    }

    @Test
    public void testDeleteValidationAccount() {
        ValidationAccount account = service.createValidationAccount(userOne);
        Long accountId = account.getId();

        service.deleteValidationAccount(account);

        ValidationAccount retrieved = service.getValidationAccountById(accountId);
        Assert.assertNull("Deleted account should not be retrievable", retrieved);
    }

    @Test
    public void testGetValidationAccountsByStatus() {
        service.createValidationAccount(userOne);
        service.createValidationAccount(userTwo, ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET);
        service.createValidationAccount(userThree, ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET);

        List<ValidationAccount> sentAccounts = service.getValidationAccountsByStatus(ValidationAccount.STATUS_SENT);

        Assert.assertNotNull("Result list should not be null", sentAccounts);
        Assert.assertEquals("Should find 3 accounts with SENT status", 3, sentAccounts.size());
    }

    @Test
    public void testIsAccountValidatedWithConfirmedAccount() {
        ValidationAccount account = service.createValidationAccount(userOne);
        account.setStatus(ValidationAccount.STATUS_CONFIRMED);
        account.setValidationReceived(Instant.now());
        service.save(account);

        boolean isValidated = service.isAccountValidated(userOne);

        Assert.assertTrue("Account with CONFIRMED status should be validated", isValidated);
    }

    @Test
    public void testIsAccountValidatedWithoutAccount() {
        boolean isValidated = service.isAccountValidated("nonExistentUser");

        Assert.assertFalse("Non-existent account should not be validated", isValidated);
    }

    @Test
    public void testIsTokenExpiredForNonPasswordResetAccount() {
        ValidationAccount account = service.createValidationAccount(userOne);
        account.setStatus(ValidationAccount.STATUS_SENT);

        boolean isExpired = service.isTokenExpired(account);

        Assert.assertFalse("Non-password reset account with SENT status should not be expired", isExpired);
    }

    @Test
    public void testIsTokenExpiredForExpiredAccount() {
        ValidationAccount account = service.createValidationAccount(userOne);
        account.setStatus(ValidationAccount.STATUS_EXPIRED);
        service.save(account);

        boolean isExpired = service.isTokenExpired(account);

        Assert.assertTrue("Account with EXPIRED status should be expired", isExpired);
    }

    @Test
    public void testIsTokenExpiredForRecentPasswordReset() {
        ValidationAccount account = service.createValidationAccount(userOne, ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET);
        account.setValidationSent(Instant.now());
        service.save(account);

        Mockito.when(serverConfigurationService.getInt("accountValidator.maxPasswordResetMinutes", 60)).thenReturn(60);

        boolean isExpired = service.isTokenExpired(account);

        Assert.assertFalse("Recently sent password reset should not be expired", isExpired);
    }

    @Test
    public void testIsTokenExpiredForOldPasswordReset() {
        ValidationAccount account = service.createValidationAccount(userOne, ValidationAccount.ACCOUNT_STATUS_PASSWORD_RESET);

        // Set validation sent to 2 hours ago
        account.setValidationSent(Instant.now().minus(2, ChronoUnit.HOURS));
        service.save(account);

        Mockito.when(serverConfigurationService.getInt("accountValidator.maxPasswordResetMinutes", 60)).thenReturn(60);

        boolean isExpired = service.isTokenExpired(account);

        Assert.assertTrue("Password reset older than max minutes should be expired", isExpired);

        // Verify the account was updated in the database
        ValidationAccount updated = service.getValidationAccountById(account.getId());
        Assert.assertEquals("Status should be updated to EXPIRED", ValidationAccount.STATUS_EXPIRED, updated.getStatus());
        Assert.assertNotNull("Validation received should be set", updated.getValidationReceived());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsTokenExpiredWithNullAccount() {
        service.isTokenExpired(null);
    }

    @Test
    public void testResendValidation() {
        ValidationAccount account = service.createValidationAccount(userOne);
        String token = account.getValidationToken();
        Integer initialSentCount = account.getValidationsSent();

        service.resendValidation(token);

        ValidationAccount updated = service.getValidationAccountBytoken(token);
        Assert.assertEquals("Validations sent count should be incremented", Integer.valueOf(initialSentCount + 1), updated.getValidationsSent());
        Assert.assertEquals("Status should be RESENT", ValidationAccount.STATUS_RESENT, updated.getStatus());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResendValidationWithInvalidToken() {
        service.resendValidation("invalid-token");
    }
}
