package com.merkle.oss.magnolia.testing.module;

import info.magnolia.license.License;
import info.magnolia.license.LicenseConsts;
import info.magnolia.license.LicenseManager;
import info.magnolia.license.LicenseStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

import org.mockito.Mockito;

import com.google.inject.Provider;

public class LicenseManagerProvider implements Provider<LicenseManager> {

    @Override
    public LicenseManager get() {
        final LicenseManager mock = Mockito.mock(LicenseManager.class);
        Mockito.doAnswer(invocationOnMock -> {
            final String moduleName = invocationOnMock.getArgument(0);
            return getLicenseStatus(moduleName);
        }).when(mock).getLicenseStatus(Mockito.anyString());
        Mockito.doAnswer(invocationOnMock -> {
            final License license = invocationOnMock.getArgument(0);
            return new LicenseStatus(LicenseStatus.STATUS_VALID, "", license);
        }).when(mock).performCheck(Mockito.any(License.class), Mockito.anyString(), Mockito.any());
        Mockito.doAnswer(invocationOnMock -> {
            final String moduleName = invocationOnMock.getArgument(0);
            return getLicense(moduleName);
        }).when(mock).getLicense(Mockito.anyString());
        Mockito.doReturn(List.of(getLicenseStatus(LicenseConsts.MODULE_ENTERPRISE))).when(mock).getAllLicenseStates();
        Mockito.doReturn(true).when(mock).isLicenseValid(Mockito.anyString());
        Mockito.doReturn(true).when(mock).isAllLicensesValid();
        return mock;
    }

    protected LicenseStatus getLicenseStatus(final String moduleName) {
        return new LicenseStatus(LicenseStatus.STATUS_VALID, "", getLicense(moduleName));
    }

    protected License getLicense(final String moduleName) {
        return new License(moduleName, "mockOwner", getProperties());
    }

    protected Properties getProperties() {
        final Properties properties = new Properties();
        properties.put("versions", "[6.1.0,100.0.0[");
        properties.put("owner", "mockOwner");
        properties.put("expiration", LocalDateTime.now().plus(Duration.ofDays(1)).format(DateTimeFormatter.ISO_DATE));
        properties.put("modules", LicenseConsts.MODULE_ENTERPRISE);
        properties.put("editions", String.join(",", LicenseConsts.EDITION_ENTERPRISE, LicenseConsts.EDITION_ENTERPRISE_PRO));
        properties.put("sites", String.valueOf(Integer.MAX_VALUE));
        return properties;
    }
}
