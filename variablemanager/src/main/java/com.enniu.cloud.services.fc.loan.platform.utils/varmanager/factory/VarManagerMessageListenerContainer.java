package com.enniu.cloud.services.fc.loan.platform.utils.varmanager.factory;


import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.VarManager;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

/**
 * @author liuyihan
 * @since 2018/6/25
 */
public class VarManagerMessageListenerContainer extends SimpleMessageListenerContainer{

    @Override
    protected void executeListener(Channel channel, Message messageIn) throws Throwable {
        try {
            VarManager.open();
            super.executeListener(channel, messageIn);
        }finally {
            VarManager.clear();
            VarManager.close();
        }
    }
}
