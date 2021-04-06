package io.interrogate.npm.pipeline.plugin;

import io.interrogate.npmyarnwrappersteps.plugin.NPMBuildWrapper;
import org.junit.Assert;
import org.junit.Test;

public class NPMBuildWrapperTest {
    @Test
    public void testGetCredentialsId() {
        NPMBuildWrapper npmBuildWrapper = new NPMBuildWrapper("credentialsId");
        String credentialsId = npmBuildWrapper.getCredentialsId();
        Assert.assertEquals("credentialsId", credentialsId);
    }

    @Test
    public void testWorkspaceSubdirectory() {
        NPMBuildWrapper npmBuildWrapper = new NPMBuildWrapper("");
        npmBuildWrapper.setWorkspaceSubdirectory("workspaceSubdirectory");
        String nodeJSVersion = npmBuildWrapper.getWorkspaceSubdirectory();
        Assert.assertEquals("workspaceSubdirectory", nodeJSVersion);
    }

}
