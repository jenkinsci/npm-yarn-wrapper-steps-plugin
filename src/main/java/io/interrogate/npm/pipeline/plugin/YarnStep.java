package io.interrogate.npm.pipeline.plugin;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.io.Serializable;

public class YarnStep extends NodeStep implements SimpleBuildStep, Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @CheckForNull
    private final String command;

    @DataBoundConstructor
    public YarnStep(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void perform(Run build, FilePath workspace, EnvVars envVars, Launcher launcher, TaskListener listener)
            throws IOException, InterruptedException {
        if (!launcher.isUnix()) {
            throw new AbortException("Only Unix systems are supported");
        }
        setUpNVM(build, workspace, envVars, launcher, listener);
        YarnUtilities.install(workspace, launcher, listener);
        YarnUtilities.addYarnToPath(envVars);
        FilePath targetDirectory = getTargetDirectory(workspace);
        boolean isInstallFromNVMRC = targetDirectory.child(".nvmrc").exists();
        ArgumentListBuilder shellCommand = NVMUtilities.getYarnCommand(command, isInstallFromNVMRC);
        doCommand(launcher, targetDirectory, envVars, shellCommand, listener.getLogger());
    }

    @Symbol("yarn")
    @Extension
    public static class DescriptorImplementation extends BuildStepDescriptor<Builder> {

        @Override
        public String getDisplayName() {
            return "Run a yarn command";
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
