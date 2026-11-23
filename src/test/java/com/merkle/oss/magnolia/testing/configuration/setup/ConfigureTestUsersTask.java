package com.merkle.oss.magnolia.testing.configuration.setup;

import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.cms.security.Realm;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class ConfigureTestUsersTask extends AbstractTask {
    private final Provider<UserManager> userManager;
    private final Provider<UserManager> systemUserManager;

    @Inject
    public ConfigureTestUsersTask(final SecuritySupport securitySupport) {
        super("ConfigureTestUsers", "Configures users used in tests");
        this.userManager = () -> securitySupport.getUserManager(Realm.REALM_ADMIN.getName());
        this.systemUserManager = () -> securitySupport.getUserManager(Realm.REALM_SYSTEM.getName());
    }

    @Override
    public void execute(final InstallContext installContext) {
        setupSystemUsers();
        setupAdminUsers();
    }

    private void setupSystemUsers() {
        final UserManager userManager = this.systemUserManager.get();
        final User superuser = userManager.getUser("superuser");
        userManager.changePassword(superuser, "superuser");
        userManager.setProperty(superuser, MgnlUserManager.PROPERTY_ENABLED, "true");
    }
    private void setupAdminUsers() {
        final UserManager userManager = this.userManager.get();
        userManager.createUser("tester", "testerPass");
    }
}
