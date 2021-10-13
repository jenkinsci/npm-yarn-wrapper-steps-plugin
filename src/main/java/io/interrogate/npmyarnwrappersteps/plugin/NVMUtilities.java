package io.interrogate.npmyarnwrappersteps.plugin;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import jenkins.tasks.SimpleBuildWrapper;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

public class NVMUtilities {

    public static final String DEFAULT_NPM_REGISTRY = "https://registry.npmjs.org/";
    public static final String DEFAULT_NODEJS_VERSION = "node";
    private static final String DEFAULT_NVM_INSTALLER_URL =
            "https://raw.githubusercontent.com/nvm-sh/nvm/v0.37.2/install.sh";
    private static final String NPM_CONFIG_COMMAND = "config set %s %s";

    @SuppressWarnings("OctalInteger")
    public static void install(FilePath workspace, Launcher launcher, TaskListener listener, String nvmInstallerUrl)
            throws IOException, InterruptedException {
        FilePath home = FilePath.getHomeDirectory(FilePath.localChannel);
        if (home.child(".nvm/nvm.sh").exists()) {
            return;
        }
        FilePath nvmInstaller = workspace.child("nvm-installer");
        nvmInstaller.copyFrom(new URL(nvmInstallerUrl));
        nvmInstaller.chmod(0755);
        launcher.launch()
                .pwd(workspace)
                .envs("NVM_METHOD=script")
                .cmdAsSingleString("bash -c ./nvm-installer")
                .stdout(listener.getLogger())
                .stderr(listener.getLogger())
                .join();
        nvmInstaller.delete();
    }

    public static void install(FilePath workspace, Launcher launcher, TaskListener listener)
            throws IOException, InterruptedException {
        install(workspace, launcher, listener, DEFAULT_NVM_INSTALLER_URL);
    }

    public static void setNVMHomeEnvironmentVariable(EnvVars envVars) {
        envVars.put("NVM_DIR", String.format("%s/.nvm", envVars.get("HOME")));
    }

    public static void setNVMHomeEnvironmentVariable(EnvVars envVars, SimpleBuildWrapper.Context context) {
        String NVM_DIR = String.format("%s/.nvm", envVars.get("HOME"));
        envVars.put("NVM_DIR", NVM_DIR);
        context.env("NVM_DIR", NVM_DIR);
    }

    public static ArgumentListBuilder getCommand(String command, String nodeJSVersion, NodeExecutor nodeExecutor) {
        String executor = "";
        switch (nodeExecutor) {
            case NPM:
                executor = "npm";
                break;
            case YARN:
                executor = "yarn";
        }

        ArgumentListBuilder commands = new ArgumentListBuilder();
        String fullCommand = String.format(
                "source \"$NVM_DIR/nvm.sh\" && nvm install %s && %s %s",
                nodeJSVersion,
                executor,
                command
        );
        commands.add("bash", "-c", fullCommand);
        return commands;

    }

    public static ArgumentListBuilder getCommand(String command, boolean isInstallFromNVMRC,
                                                 NodeExecutor nodeExecutor) {
        String nodeJSVersion = isInstallFromNVMRC ? "" : NVMUtilities.DEFAULT_NODEJS_VERSION;
        return getCommand(command, nodeJSVersion, nodeExecutor);
    }

    public static ArgumentListBuilder getNPMCommand(String command, boolean isInstallFromNVMRC) {
        return getCommand(command, isInstallFromNVMRC, NodeExecutor.NPM);
    }

    public static ArgumentListBuilder getYarnCommand(String command, boolean isInstallFromNVMRC) {
        return getCommand(command, isInstallFromNVMRC, NodeExecutor.YARN);
    }

    public static void setNPMConfig(String key, String value, String workspaceSubdirectory, FilePath workspace,
                                    Launcher launcher, PrintStream logger, EnvVars envVars)
            throws IOException, InterruptedException {
        FilePath targetDirectory = getTargetDirectory(workspace, workspaceSubdirectory);
        boolean isInstallFromNVMRC = false;
        if (targetDirectory.child(".nvmrc").exists()) {
            isInstallFromNVMRC = true;
        }
        ArgumentListBuilder _authCommand = NVMUtilities
                .getCommand(String.format(NPM_CONFIG_COMMAND, key, value), isInstallFromNVMRC,
                        NVMUtilities.NodeExecutor.NPM);
        int statusCode = launcher.launch()
                .quiet(true)
                .envs(envVars)
                .pwd(workspace)
                .cmds(_authCommand)
                .stdout(logger).join();
        if (statusCode != 0) {
            throw new AbortException("");
        }
    }

    public static FilePath getTargetDirectory(FilePath workspace, String workspaceSubdirectory)
            throws IOException, InterruptedException {
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

    public enum NodeExecutor {NPM, YARN}
}
