package io.interrogate.npm.pipeline.plugin;

import org.junit.Assert;
import org.junit.Test;

public class YarnStepTest {

    @Test
    public void testCommand() {
        YarnStep yarnStep = new YarnStep("command");
        String command = yarnStep.getCommand();
        Assert.assertEquals("command", command);
    }
}
