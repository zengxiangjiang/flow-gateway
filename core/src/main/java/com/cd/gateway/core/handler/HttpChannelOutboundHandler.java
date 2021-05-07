package com.cd.gateway.core.handler;

import io.netty.channel.*;

import java.net.SocketAddress;

/**
 * http响应时的处理
 * @author zengxj
 * @date 2021/05/07
 */
public class HttpChannelOutboundHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        super.read(ctx);
    }
}
