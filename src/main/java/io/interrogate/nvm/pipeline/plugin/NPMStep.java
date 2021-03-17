package io.interrogate.nvm.pipeline.plugin;

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

public class NPMStep extends Builder implements SimpleBuildStep {
    private final String command;

    @DataBoundConstructor
    public NPMStep(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public void perform(Run build, FilePath workspace, EnvVars envVars, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
        if (!launcher.isUnix()) {
            throw new AbortException("Only Unix systems are supported");
        }
        NVMUtilities.install(workspace, launcher, listener);
        PrintStream logger = listener.getLogger();
        envVars.put("NVM_DIR", envVars.get("HOME") + "/.nvm");
        FilePath nvmrcFilePath = workspace.child(".nvmrc");
        boolean isInstallFromNVMRC = nvmrcFilePath.exists();
        ArgumentListBuilder shellCommand = NVMUtilities.getNPMCommand(command, isInstallFromNVMRC);
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

    @Symbol("npm")
    @Extension
    public static class DescriptorImplementation extends BuildStepDescriptor<Builder> {

        @Override
        public String getDisplayName() {
            return "Run an npm command";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
