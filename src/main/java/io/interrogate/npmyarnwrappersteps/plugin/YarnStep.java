package io.interrogate.npmyarnwrappersteps.plugin;

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
import java.io.File;
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
            throw new AbortException(Messages.Error_OnlyUnixSystemsAreSupported());
        }
        setUpNVM(build, workspace, envVars, launcher, listener);
        setUpYarn(build, workspace, envVars, launcher, listener);
        FilePath targetDirectory = getTargetDirectory(workspace);
        boolean isInstallFromNVMRC = targetDirectory.child(".nvmrc").exists();
        ArgumentListBuilder shellCommand = NVMUtilities.getYarnCommand(command, isInstallFromNVMRC);
        doCommand(launcher, targetDirectory, envVars, shellCommand, listener.getLogger());
    }

    private void setUpYarn(Run build, FilePath workspace, EnvVars envVars, Launcher launcher, TaskListener listener)
            throws IOException, InterruptedException {
        String isYarnSetupEnvVariable = envVars.get(String.format(NPMBuildWrapper.JENKINS_YARN_SETUP_FOR_BUILD_S, build.getId()));
        boolean isYarnSetup = isYarnSetupEnvVariable != null && isYarnSetupEnvVariable.equals("TRUE");
        if (!isYarnSetup) {
            YarnUtilities.install(workspace, launcher, listener);
        }
        YarnUtilities.addYarnToPath(envVars);
    }

    @Symbol("yarn")
    @Extension
    public static class DescriptorImplementation extends BuildStepDescriptor<Builder> {

        @Override
        public String getDisplayName() {
            return Messages.YarnStep_RunAYarnCommand();
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
