package testutil;

import org.junit.Before;
import org.mockito.MockitoAnnotations;

/**
 * Created by yeandang on 2017/6/29.
 */
public class MockBase {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        postSetUp();
    }

    public void postSetUp() {
    }

}
