package io.interrogate.npm.pipeline.plugin;

import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.springframework.lang.NonNull;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;

public class NPMStep extends Builder implements SimpleBuildStep, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @CheckForNull
    private final String command;

    private String workspaceSubdirectory = "";

    @DataBoundConstructor
    public NPMStep(String command) {
        this.command = command;
    }

    @NonNull
    public String getWorkspaceSubdirectory() {
        return workspaceSubdirectory;
    }

    @DataBoundSetter
    public void setWorkspaceSubdirectory(String workspaceSubdirectory) {
        this.workspaceSubdirectory = workspaceSubdirectory;
    }

    @NonNull
    public String getCommand() {
        return command;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void perform(Run build, FilePath workspace, EnvVars envVars, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
        if (!launcher.isUnix()) {
            throw new AbortException("Only Unix systems are supported");
        }
        NVMUtilities.install(workspace, launcher, listener);
        NVMUtilities.setNVMHomeEnvironmentVariable(envVars);
        PrintStream logger = listener.getLogger();
        FilePath targetDirectory = workspace;
        if (StringUtils.isNotBlank(workspaceSubdirectory)) {
            targetDirectory = workspace.child(workspaceSubdirectory);
            if (!targetDirectory.exists()) {
                throw new AbortException(String.format("%s does not exist", targetDirectory.toURI().getPath()));
            }
            if (!targetDirectory.isDirectory()) {
                throw new AbortException(String.format("%s is not a directory", targetDirectory.toURI().getPath()));
            }
        }
        FilePath nvmrcFilePath = targetDirectory.child(".nvmrc");
        boolean isInstallFromNVMRC = nvmrcFilePath.exists();
        ArgumentListBuilder shellCommand = NVMUtilities.getNPMCommand(command, isInstallFromNVMRC);
        Integer statusCode = launcher.launch()
                .pwd(targetDirectory)
                .quiet(true)
                .envs(envVars)
                .cmds(shellCommand)
                .stdout(logger).stderr(logger).join();
        if (statusCode != 0) {
            throw new AbortException("");
        }
    }

    @Symbol("npm")
    @Extension
    public static class DescriptorImplementation extends BuildStepDescriptor<Builder> {

        @Override
        public String getDisplayName() {
            return "Run an npm command";
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
