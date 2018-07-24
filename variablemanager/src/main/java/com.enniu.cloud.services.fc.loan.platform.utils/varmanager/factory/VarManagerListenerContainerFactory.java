package com.enniu.cloud.services.fc.loan.platform.utils.varmanager.factory;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

/**
 * @author liuyihan
 * @since 2018/6/25
 */
public class VarManagerListenerContainerFactory extends SimpleRabbitListenerContainerFactory{

    @Override
    protected SimpleMessageListenerContainer createContainerInstance() {
        return new VarManagerMessageListenerContainer();
    }
}
