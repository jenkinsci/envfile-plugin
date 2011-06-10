package hudson.plugins.envfile;

import org.jvnet.hudson.test.HudsonTestCase;

import java.io.IOException;
import java.io.File;

import hudson.model.FreeStyleProject;
import hudson.tasks.BuildWrapper;

/**
 * Created by IntelliJ IDEA.
 * User: kedar
 * Date: May 29, 2010
 * Time: 8:21:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class EnvFileBuildWrapperTest extends HudsonTestCase {

    public void testBasic() throws IOException {
        FreeStyleProject project = super.createFreeStyleProject();
        File tf = new File(hudson.getRootDir(), "test.env");
        BuildWrapper bw = new EnvFileBuildWrapper(tf.getAbsolutePath());
        //possibly, come back here and complete the test with higher version of hudson (e.g. getBuildWrapperList)
    }
}
