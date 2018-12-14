package com.github.shk0da.micro.visa2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import com.github.shk0da.micro.main.service.AbstractTcpServerService;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RefreshScope
public class TcpServerService extends AbstractTcpServerService {

    @Autowired
    private Visa2Service visa2Service;

    @Override
    protected byte[] processingIncomingMessage(byte[] data, AtomicBoolean cancelled) {
        return visa2Service.processingIncomingMessage(data, cancelled);
    }
}
