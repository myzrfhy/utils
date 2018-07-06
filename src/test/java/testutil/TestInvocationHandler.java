package testutil;

import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.thread.VarManagerInvocationHandler;
import lombok.NoArgsConstructor;

/**
 * @author liuyihan
 * @since 2018/7/6
 */
@NoArgsConstructor
public class TestInvocationHandler extends VarManagerInvocationHandler {

    @Override
    protected void beforeInvoke() {
        super.beforeInvoke();
        System.out.println("TestInvocationHandler");
    }
}
