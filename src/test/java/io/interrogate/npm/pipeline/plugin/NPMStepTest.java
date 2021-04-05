package io.interrogate.npm.pipeline.plugin;

import io.interrogate.npmyarnwrappersteps.plugin.NPMStep;
import org.junit.Assert;
import org.junit.Test;

public class NPMStepTest {
    @Test
    public void testCommand() {
        NPMStep npmStep = new NPMStep("command");
        String command = npmStep.getCommand();
        Assert.assertEquals("command", command);
    }
}
