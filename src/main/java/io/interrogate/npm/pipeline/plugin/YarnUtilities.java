package io.interrogate.npm.pipeline.plugin;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;

import java.io.IOException;
import java.net.URL;

public class YarnUtilities {

    private static final String DEFAULT_YARN_INSTALLER_URL = "https://yarnpkg.com/install.sh";
    private static final String YARN_PATH_TEMPLATE = "%s/.yarn/bin:%s/.config/yarn/global/node_modules/.bin:%s";

    public static void install(FilePath workspace, Launcher launcher, TaskListener listener, String yarnInstallerUrl)
            throws IOException, InterruptedException {
        FilePath home = FilePath.getHomeDirectory(FilePath.localChannel);
        if (home.child(".yarn").exists()) {
            return;
        }
        FilePath yarnInstaller = workspace.child("yarn-installer");
        yarnInstaller.copyFrom(new URL(yarnInstallerUrl));
        yarnInstaller.chmod(0755);
        launcher.launch()
                .pwd(workspace)
                .cmdAsSingleString("bash -c ./yarn-installer")
                .stdout(listener.getLogger())
                .stderr(listener.getLogger())
                .join();
        yarnInstaller.delete();
    }

    public static void install(FilePath workspace, Launcher launcher, TaskListener listener)
            throws IOException, InterruptedException {
        install(workspace, launcher, listener, DEFAULT_YARN_INSTALLER_URL);
    }

    public static void addYarnToPath(EnvVars envVars) {
        envVars.replace("PATH",
                String.format(YARN_PATH_TEMPLATE, envVars.get("HOME"), envVars.get("HOME"), envVars.get("PATH")));
    }
}
