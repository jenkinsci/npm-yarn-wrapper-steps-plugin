package io.interrogate.npmyarnwrappersteps.plugin;

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
import io.interrogate.npmyarnwrappersteps.plugin.credentials.NPMCredentialsImplementation;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildWrapper;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.springframework.lang.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NPMBuildWrapper extends SimpleBuildWrapper implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final String credentialsId;
    private String npmRegistry = NVMUtilities.DEFAULT_NPM_REGISTRY;
    private String workspaceSubdirectory = "";

    public static final String JENKINS_NPM_WORKSPACE_SUBDIRECTORY = "JENKINS_NPM_WORKSPACE_SUBDIRECTORY";
    public static final String JENKINS_NVM_SETUP_FOR_BUILD_S = "JENKINS_NVM_SETUP_FOR_BUILD_%s";

    @DataBoundConstructor
    public NPMBuildWrapper(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getWorkspaceSubdirectory() {
        return workspaceSubdirectory;
    }

    @DataBoundSetter
    public void setWorkspaceSubdirectory(String workspaceSubdirectory) {
        this.workspaceSubdirectory = workspaceSubdirectory;
    }

    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    @Override
    public void setUp(Context context, Run build, FilePath workspace, Launcher launcher, TaskListener listener,
                      EnvVars envVars) throws IOException, InterruptedException {
        if (!launcher.isUnix()) {
            throw new AbortException(Messages.Error_OnlyUnixSystemsAreSupported());
        }
        PrintStream logger = listener.getLogger();
        String npmConfigUserConfig = String.format("%s/.npmrc", envVars.get("WORKSPACE_TMP"));
        envVars.put("NPM_CONFIG_USERCONFIG", npmConfigUserConfig);
        context.env("NPM_CONFIG_USERCONFIG", npmConfigUserConfig);
        if (StringUtils.isNotBlank(workspaceSubdirectory)) {
            context.env(NPMBuildWrapper.JENKINS_NPM_WORKSPACE_SUBDIRECTORY, workspaceSubdirectory);
        }
        List<String> tempConfigFiles = Arrays.asList(npmConfigUserConfig);
        context.setDisposer(new CleanupDisposer(new HashSet<>(tempConfigFiles)));
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
        NVMUtilities.setNVMHomeEnvironmentVariable(envVars, context);
        NVMUtilities.setNPMConfig("registry", npmRegistry, workspaceSubdirectory, workspace, launcher, logger, envVars);
        if (_auth != null && email != null) {
            NVMUtilities.setNPMConfig("email", email, workspaceSubdirectory, workspace, launcher, logger, envVars);
            NVMUtilities.setNPMConfig("_auth", _auth, workspaceSubdirectory, workspace, launcher, logger, envVars);
        }
        context.env(String.format(NPMBuildWrapper.JENKINS_NVM_SETUP_FOR_BUILD_S, build.getId()), "TRUE");
    }

    @Symbol("withNPMWrapper")
    @Extension
    public static class DescriptorImplementation extends BuildWrapperDescriptor {

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.NPMBuildWrapper_SetNPMEnvironment();
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
            StandardListBoxModel result = new StandardListBoxModel();
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(credentialsId);
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ) &&
                        !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(credentialsId);
                }
            }
            List<NPMCredentialsImplementation> credentialsList = CredentialsProvider
                    // TODO: replace ACL.SYSTEM with something not deprecated
                    .lookupCredentials(NPMCredentialsImplementation.class, item, ACL.SYSTEM, Collections.emptyList());
            result.includeEmptyValue();
            for (NPMCredentialsImplementation credential : credentialsList) {
                result.add(credential.getId());
            }
            result.includeCurrentValue(credentialsId);
            return result;
        }
    }

    private static class CleanupDisposer extends Disposer {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        private final Set<String> tempFiles;

        public CleanupDisposer(Set<String> tempFiles) {
            this.tempFiles = tempFiles;
        }

        @Override
        public void tearDown(Run build, FilePath workspace, Launcher launcher, TaskListener listener)
                throws IOException, InterruptedException {
            for (String tempFile : tempFiles) {
                FilePath tempFilePath = new FilePath(new File(tempFile));
                if (tempFilePath.exists()) {
                    tempFilePath.delete();
                }
            }
        }
    }
}