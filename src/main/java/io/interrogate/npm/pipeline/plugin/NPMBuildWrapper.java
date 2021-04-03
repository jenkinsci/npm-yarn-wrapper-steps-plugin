package io.interrogate.npm.pipeline.plugin;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.ListBoxModel;
import io.interrogate.npm.pipeline.plugin.credentials.NPMCredentialsImplementation;
import jenkins.tasks.SimpleBuildWrapper;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class NPMBuildWrapper extends SimpleBuildWrapper implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String credentialsId = "";
    private String nodeJSVersion = NVMUtilities.DEFAULT_NODEJS_VERSION;
    private String npmRegistry = NVMUtilities.DEFAULT_NPM_REGISTRY;

    @DataBoundConstructor
    public NPMBuildWrapper(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @NonNull
    public String getNodeJSVersion() {
        return nodeJSVersion;
    }

    @DataBoundSetter
    public void setNodeJSVersion(String nodeJSVersion) {
        this.nodeJSVersion = nodeJSVersion;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setUp(Context context, Run build, FilePath workspace, Launcher launcher, TaskListener listener,
                      EnvVars envVars) throws IOException, InterruptedException {
        if (!launcher.isUnix()) {
            throw new AbortException("Only Unix systems are supported");
        }
        PrintStream logger = listener.getLogger();
        NPMCredentialsImplementation credential = CredentialsProvider
                .findCredentialById(credentialsId, NPMCredentialsImplementation.class, build, Collections.emptyList());
        String _auth = null;
        String email = null;
        if (credential != null) {
            npmRegistry = credential.getRegistry();
            _auth = Base64.getEncoder().encodeToString(
                    String.format("%s:%s", credential.getUsername(), credential.getPassword().getPlainText()).getBytes(
                            StandardCharsets.UTF_8));
            email = credential.getUserEmail();
        }
        NVMUtilities.install(workspace, launcher, listener);
        NVMUtilities.setNVMHomeEnvironmentVariable(envVars);
        NVMUtilities.setNPMConfig("registry", npmRegistry, nodeJSVersion, workspace, launcher, logger, envVars);
        if (_auth != null && email != null) {
            NVMUtilities.setNPMConfig("email", email, nodeJSVersion, workspace, launcher, logger, envVars);
            NVMUtilities.setNPMConfig("_auth", _auth, nodeJSVersion, workspace, launcher, logger, envVars);
        }
    }

    @Symbol("npmWrapper")
    @Extension
    public static class DescriptorImplementation extends BuildWrapperDescriptor {

        @Override
        public String getDisplayName() {
            return "Set NPM Environment";
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentials) {
            List<NPMCredentialsImplementation> credentialsList = CredentialsProvider
                    // TODO: replace ACL.SYSTEM with something not deprecated
                    .lookupCredentials(NPMCredentialsImplementation.class, item, ACL.SYSTEM, Collections.emptyList());
            ListBoxModel result = new StandardListBoxModel()
                    .includeEmptyValue()
                    .includeCurrentValue(credentials);
            for (NPMCredentialsImplementation credential : credentialsList) {
                result.add(credential.getId());
            }
            return result;
        }
    }
}
