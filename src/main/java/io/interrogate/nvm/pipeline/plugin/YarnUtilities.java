package io.interrogate.nvm.pipeline.plugin;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;

import java.io.IOException;
import java.net.URL;

public class YarnUtilities {

    private static final String DEFAULT_YARN_INSTALLER_URL = "https://yarnpkg.com/install.sh";

    public static void install(FilePath workspace, Launcher launcher, TaskListener listener, String yarnInstallerUrl) throws IOException, InterruptedException {
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

    public static void install(FilePath workspace, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
        install(workspace, launcher, listener, DEFAULT_YARN_INSTALLER_URL);
    }
}
