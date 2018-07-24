package testutil;

import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.ThreadLocalManager;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author liuyihan
 * @since 2018/1/3
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({ThreadLocalManager.class})
public abstract class PowerMockBase {

    @Before
    public void setUp() {
        postSetUp();
    }

    protected void postSetUp(){
        PowerMockito.mockStatic(ThreadLocalManager.class);
    }
}
