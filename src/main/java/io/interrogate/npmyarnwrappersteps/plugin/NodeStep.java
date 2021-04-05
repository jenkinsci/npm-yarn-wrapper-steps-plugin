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
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.io.PrintStream;

public abstract class NodeStep extends Builder {

    private String workspaceSubdirectory = "";

    @NonNull
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

    FilePath getTargetDirectory(FilePath workspace) throws IOException, InterruptedException {
        FilePath targetDirectory = workspace;
        if (StringUtils.isNotBlank(workspaceSubdirectory)) {
            targetDirectory = workspace.child(workspaceSubdirectory);
            if (!targetDirectory.exists()) {
                throw new AbortException(String.format(Messages.NodeStep_targetDirectoryDoesNotExist(),
                        targetDirectory.toURI().getPath()));
            }
            if (!targetDirectory.isDirectory()) {
                throw new AbortException(String.format(Messages.NodeStep_targetDirectoryIsNotADirectory(),
                        targetDirectory.toURI().getPath()));
            }
        }
        return targetDirectory;
    }

    @SuppressWarnings("rawtypes")
    public void setUpNVM(Run build, FilePath workspace, EnvVars envVars, Launcher launcher, TaskListener listener)
            throws IOException, InterruptedException {
        boolean isNVMSetup = envVars.get(String.format("JENKINS_NVM_SETUP_FOR_BUILD_%s", build.getId())) == "TRUE";
        if (!isNVMSetup) {
            NVMUtilities.install(workspace, launcher, listener);
            NVMUtilities.setNVMHomeEnvironmentVariable(envVars);
        }
    }
}
