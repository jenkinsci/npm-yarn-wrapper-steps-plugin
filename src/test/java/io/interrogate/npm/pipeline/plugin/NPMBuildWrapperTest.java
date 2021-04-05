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
    public void testNodeJSVersion() {
        NPMBuildWrapper npmBuildWrapper = new NPMBuildWrapper("");
        npmBuildWrapper.setNodeJSVersion("nodeJSVersion");
        String nodeJSVersion = npmBuildWrapper.getNodeJSVersion();
        Assert.assertEquals("nodeJSVersion", nodeJSVersion);
    }

}
