package io.interrogate.npmyarnwrappersteps.plugin.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import hudson.Extension;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class NPMCredentialsImplementation extends BaseStandardCredentials implements NPMCredentials {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final String registry;
    private final String userEmail;
    private final String username;
    private final Secret password;

    @DataBoundConstructor
    public NPMCredentialsImplementation(
            @CheckForNull CredentialsScope scope,
            @CheckForNull String id,
            @Nonnull String registry,
            @Nonnull String userEmail,
            @Nonnull String username,
            @Nonnull String password,
            @CheckForNull String description
    ) {
        super(scope, id, description);
        this.registry = registry;
        this.userEmail = userEmail;
        this.username = username;
        this.password = Secret.fromString(password);
    }

    @Override
    public String getRegistry() {
        return registry;
    }

    @Override
    public String getUserEmail() {
        return userEmail;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Secret getPassword() {
        return password;
    }

    @Extension
    public static class DescriptorImplementation extends BaseStandardCredentialsDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.NPMCredentialsImplementation_NPMLoginCredentials();
        }

    }
}
