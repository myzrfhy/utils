package com.enniu.cloud.services.fc.loan.platform.utils.thread;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.VarManager;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import testutil.MockBase;

/**
 * @author liuyihan
 * @since 2018/6/26
 */
public class VarManagerTest extends MockBase{

    @Mock
    Print print;

    @Override
    public void postSetUp() {
        super.postSetUp();

    }

    @Test
    public void switch_close(){
        String methodKey = "switch_close";
        doReturn(methodKey).when(print).test(eq(methodKey));

        VarManager.close();

        VarManager.apply(methodKey, ()->print.test(methodKey));
        VarManager.apply(methodKey, ()->print.test(methodKey));

        verify(print, times(2)).test(eq(methodKey));
    }

    @Test
    public void switch_open(){
        String methodKey = "switch_open";
        doReturn(methodKey).when(print).test(eq(methodKey));


        VarManager.open();

        VarManager.apply(methodKey, ()->print.test(methodKey));
        VarManager.apply(methodKey, ()->print.test(methodKey));

        VarManager.clear();
        VarManager.close();

        verify(print, times(1)).test(eq(methodKey));
    }

    @Test
    public void switch_close_bi(){
        String methodKey = "switch_close";
        doReturn(methodKey).when(print).test2(eq(methodKey), eq(methodKey));


        VarManager.close();

        VarManager.apply(methodKey, ()->print.test2(methodKey, methodKey));
        VarManager.apply(methodKey, ()->print.test2(methodKey, methodKey));

        verify(print, times(2)).test2(eq(methodKey),eq(methodKey));
    }

    @Test
    public void switch_open_bi(){
        String methodKey = "switch_open";
        doReturn(methodKey).when(print).test2(eq(methodKey), eq(methodKey));


        VarManager.open();

        VarManager.apply(methodKey, ()->print.test2(methodKey, methodKey));
        VarManager.apply(methodKey, ()->print.test2(methodKey, methodKey));

        VarManager.clear();
        VarManager.close();

        verify(print, times(1)).test2(eq(methodKey), eq(methodKey));
    }

    @Test
    public void remove(){
        VarManager.open();
        VarManager.put("aaa", "ssss");
        Assert.assertEquals("ssss", VarManager.get("aaa"));
        VarManager.remove("aaa");
        Assert.assertNull(VarManager.get("aaa"));
        VarManager.close();
    }

    @Test
    public void clear(){
        VarManager.open();
        String key = String.valueOf(System.currentTimeMillis());
        Long value = System.currentTimeMillis();
        VarManager.put(String.valueOf(key), value);
        VarManager.clear();
        VarManager.get(key);
        VarManager.close();
    }

    @Test
    public void threadLocalClear(){
        VarManager.open();
        String key = String.valueOf(System.currentTimeMillis());
        Long value = System.currentTimeMillis();
        VarManager.put(String.valueOf(key), value);
        VarManager.threadLocalClear();
        VarManager.get(key);
        VarManager.close();
    }

    private class Print{
        public String test(String name){
            System.out.println(name);
            return name;
        }

        public String test2(String name, String name2){
            System.out.println(name+name2);
            return name;
        }
    }

}
