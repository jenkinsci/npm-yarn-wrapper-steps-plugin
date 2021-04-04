package io.interrogate.npm.pipeline.plugin;

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
