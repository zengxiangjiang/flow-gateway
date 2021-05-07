package com.cd.gateway.core.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import org.springframework.beans.factory.annotation.Value;

/**
 * @Author zengxj
 * @date  2021/5/7 22:18
 */
public class ChannelHandlerInitializer extends ChannelInitializer<SocketChannel> {

    @Value("${netty.maxContentLength:2000}")
    private int maxContentLength;
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpResponseDecoder());
        pipeline.addLast(new HttpChannelInboundHandler());
        pipeline.addLast(new HttpChannelOutboundHandler());
        //netty默认2000kb
        pipeline.addLast(new HttpObjectAggregator(maxContentLength*1024));
    }
}
