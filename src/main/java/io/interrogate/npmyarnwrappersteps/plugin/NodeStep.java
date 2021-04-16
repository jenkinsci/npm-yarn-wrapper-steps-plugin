package io.interrogate.npmyarnwrappersteps.plugin;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;

public abstract class NodeStep extends Builder {

    private String workspaceSubdirectory = "";

    @Nonnull
    public String getWorkspaceSubdirectory() {
        return workspaceSubdirectory;
    }

    @DataBoundSetter
    public void setWorkspaceSubdirectory(String workspaceSubdirectory) {
        this.workspaceSubdirectory = workspaceSubdirectory;
    }

    void doCommand(Launcher launcher, FilePath targetDirectory, EnvVars envVars, ArgumentListBuilder shellCommand,
                   PrintStream logger)
            throws IOException, InterruptedException {
        int statusCode = launcher.launch()
                .pwd(targetDirectory)
                .quiet(true)
                .envs(envVars)
                .cmds(shellCommand)
                .stdout(logger).stderr(logger).join();
        if (statusCode != 0) {
            throw new AbortException("");
        }
    }

    FilePath getTargetDirectory(FilePath workspace) throws IOException, InterruptedException {
        return NVMUtilities.getTargetDirectory(workspace, workspaceSubdirectory);
    }

    @SuppressWarnings("rawtypes")
    public void setUpNVM(Run build, FilePath workspace, EnvVars envVars, Launcher launcher, TaskListener listener)
            throws IOException, InterruptedException {
        boolean isNVMSetup =
                envVars.get(String.format(NPMBuildWrapper.JENKINS_NVM_SETUP_FOR_BUILD_S, build.getId())).equals("TRUE");
        if (!isNVMSetup) {
            NVMUtilities.install(workspace, launcher, listener);
            NVMUtilities.setNVMHomeEnvironmentVariable(envVars);
        }
        String JENKINS_NPM_WORKSPACE_SUBDIRECTORY = envVars.get(NPMBuildWrapper.JENKINS_NPM_WORKSPACE_SUBDIRECTORY);
        if (StringUtils.isNotBlank(JENKINS_NPM_WORKSPACE_SUBDIRECTORY)) {
            setWorkspaceSubdirectory(JENKINS_NPM_WORKSPACE_SUBDIRECTORY);
        }
    }
}
