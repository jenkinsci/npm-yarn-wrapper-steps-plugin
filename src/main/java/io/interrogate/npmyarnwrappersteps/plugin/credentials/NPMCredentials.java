package io.interrogate.npmyarnwrappersteps.plugin.credentials;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import hudson.util.Secret;

public interface NPMCredentials extends StandardCredentials {
    String getRegistry();

    String getUserEmail();

    String getUsername();

    Secret getPassword();
}
