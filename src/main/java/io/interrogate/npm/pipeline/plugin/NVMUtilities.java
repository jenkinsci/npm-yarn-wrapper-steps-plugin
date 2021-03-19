package io.interrogate.npm.pipeline.plugin;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;

import java.io.IOException;
import java.net.URL;

public class NVMUtilities {

    private static final String DEFAULT_NVM_INSTALLER_URL = "https://raw.githubusercontent.com/nvm-sh/nvm/v0.37.2/install.sh";

    public static void install(FilePath workspace, Launcher launcher, TaskListener listener, String nvmInstallerUrl) throws IOException, InterruptedException {
        FilePath home = FilePath.getHomeDirectory(FilePath.localChannel);
        if (home.child(".nvm/nvm.sh").exists()) {
            return;
        }
        FilePath nvmInstaller = workspace.child("nvm-installer");
        nvmInstaller.copyFrom(new URL(nvmInstallerUrl));
        nvmInstaller.chmod(0755);
        launcher.launch()
                .pwd(workspace)
                .cmdAsSingleString("bash -c ./nvm-installer")
                .stdout(listener.getLogger())
                .stderr(listener.getLogger())
                .join();
        nvmInstaller.delete();
    }

    public static void install(FilePath workspace, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
        install(workspace, launcher, listener, DEFAULT_NVM_INSTALLER_URL);
    }

    public static ArgumentListBuilder getCommand(String command, boolean isInstallFromNVMRC, NodeExecutor nodeExecutor) {
        String executor = "";
        switch (nodeExecutor) {
            case NPM:
                executor = "npm";
                break;
            case YARN:
                executor = "yarn";
        }

        ArgumentListBuilder commands = new ArgumentListBuilder();
        String nodeInstall = isInstallFromNVMRC ? "" : "node";
        String fullCommand = String.format(
                "source \"$NVM_DIR/nvm.sh\" && nvm install %s && %s %s",
                nodeInstall,
                executor,
                command
        );
        commands.add("bash", "-c", fullCommand);
        return commands;

    }

    public static ArgumentListBuilder getNPMCommand(String command, boolean isInstallFromNVMRC) {
        return getCommand(command, isInstallFromNVMRC, NodeExecutor.NPM);
    }

    public static ArgumentListBuilder getYarnCommand(String command, boolean isInstallFromNVMRC) {
        return getCommand(command, isInstallFromNVMRC, NodeExecutor.YARN);
    }

    public enum NodeExecutor {NPM, YARN}
}
