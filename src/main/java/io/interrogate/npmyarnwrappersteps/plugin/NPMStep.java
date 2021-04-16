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
import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public class NPMStep extends NodeStep implements SimpleBuildStep, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @CheckForNull
    private final String command;

    @DataBoundConstructor
    public NPMStep(@CheckForNull String command) {
        this.command = command;
    }

    @Nonnull
    public String getCommand() {
        return Objects.requireNonNull(command);
    }

    @Override
    public void perform(@Nonnull Run build, @Nonnull FilePath workspace, @Nonnull EnvVars envVars,
                        @Nonnull Launcher launcher, @Nonnull TaskListener listener)
            throws IOException, InterruptedException {
        if (!launcher.isUnix()) {
            throw new AbortException(Messages.Error_OnlyUnixSystemsAreSupported());
        }
        setUpNVM(build, workspace, envVars, launcher, listener);
        FilePath targetDirectory = getTargetDirectory(workspace);
        boolean isInstallFromNVMRC = targetDirectory.child(".nvmrc").exists();
        ArgumentListBuilder shellCommand = NVMUtilities.getNPMCommand(command, isInstallFromNVMRC);
        doCommand(launcher, targetDirectory, envVars, shellCommand, listener.getLogger());
    }

    @Symbol("npm")
    @Extension
    public static class DescriptorImplementation extends BuildStepDescriptor<Builder> {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.NPMStep_RunAnNPMCommand();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
