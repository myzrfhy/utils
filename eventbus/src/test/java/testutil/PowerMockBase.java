package testutil;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author liuyihan
 * @since 2018/1/3
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({})
public abstract class PowerMockBase {

    @Before
    public void setUp() {
        postSetUp();
    }

    protected void postSetUp(){

    }
}
