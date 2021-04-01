package io.interrogate.npm.pipeline.plugin;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.ArgumentListBuilder;
import hudson.util.ListBoxModel;
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
import java.util.Collections;

public class NPMBuildWrapper extends SimpleBuildWrapper implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final String NPM_CONFIG_REGISTRY_COMMAND = "config set registry %s";

    private String credentialsId = "";
    private String npmRegistry = NVMUtilities.DEFAULT_NPM_REGISTRY;
    private String nodeJSVersion = NVMUtilities.DEFAULT_NODEJS_VERSION;

    @DataBoundConstructor
    public NPMBuildWrapper(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @NonNull
    public String getNpmRegistry() {
        return npmRegistry;
    }

    @DataBoundSetter
    public void setNpmRegistry(String npmRegistry) {
        this.npmRegistry = npmRegistry;
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
    public void setUp(Context context, Run build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars envVars) throws IOException, InterruptedException {
        context.env("NPM_REGISTRY", npmRegistry);
        context.env("NODEJS_VERSION", nodeJSVersion);
        NVMUtilities.install(workspace, launcher, listener);
        NVMUtilities.setNVMHomeEnvironmentVariable(envVars);
        PrintStream logger = listener.getLogger();
        ArgumentListBuilder configCommand = NVMUtilities.getCommand(String.format(NPM_CONFIG_REGISTRY_COMMAND, npmRegistry), nodeJSVersion, NVMUtilities.NodeExecutor.NPM);
        Integer statusCode = launcher.launch()
                .quiet(true)
                .envs(envVars)
                .pwd(workspace)
                .cmds(configCommand)
                .stdout(logger)
                .stderr(logger).join();

        if (statusCode != 0) {
            throw new AbortException("");
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
            return new StandardListBoxModel()
                    .includeEmptyValue()
                    .includeMatching(item, StandardUsernamePasswordCredentials.class, Collections.emptyList(), CredentialsMatchers.always())
                    .includeCurrentValue(credentials);
        }
    }
}
