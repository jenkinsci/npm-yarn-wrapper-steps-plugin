package io.interrogate.npm.pipeline.plugin;

import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.PrintStream;

public class YarnStep extends Builder implements SimpleBuildStep {
    private final String command;
    private final String YARN_PATH_TEMPLATE = "%s/.yarn/bin:%s/.config/yarn/global/node_modules/.bin:%s";

    @DataBoundConstructor
    public YarnStep(String command) {
        this.command = command;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void perform(Run build, FilePath workspace, EnvVars envVars, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
        if (!launcher.isUnix()) {
            throw new AbortException("Only Unix systems are supported");
        }
        NVMUtilities.install(workspace, launcher, listener);
        YarnUtilities.install(workspace, launcher, listener);
        PrintStream logger = listener.getLogger();
        envVars.put("NVM_DIR", envVars.get("HOME") + "/.nvm");
        envVars.replace("PATH", String.format(YARN_PATH_TEMPLATE, envVars.get("HOME"), envVars.get("HOME"), envVars.get("PATH")));
        FilePath nvmrcFilePath = workspace.child(".nvmrc");
        boolean isInstallFromNVMRC = nvmrcFilePath.exists();
        ArgumentListBuilder shellCommand = NVMUtilities.getYarnCommand(command, isInstallFromNVMRC);
        Integer statusCode = launcher.launch()
                .pwd(workspace)
                .quiet(true)
                .envs(envVars)
                .cmds(shellCommand)
                .stdout(logger).stderr(logger).join();
        if (statusCode != 0) {
            throw new AbortException("");
        }
    }

    public String getCommand() {
        return command;
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
