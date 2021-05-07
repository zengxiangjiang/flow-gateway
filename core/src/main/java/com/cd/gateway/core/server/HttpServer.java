package com.cd.gateway.core.server;

import com.cd.gateway.common.exception.GatewayException;
import com.cd.gateway.core.handler.ChannelHandlerInitializer;
import com.cd.gateway.core.handler.HttpChannelInboundHandler;
import com.cd.gateway.core.handler.HttpChannelOutboundHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetSocketAddress;

/**
 * http server
 *
 * @author zengxj
 * @date 2021/05/07
 */
public class HttpServer {
    private static HttpServer httpServer = new HttpServer();
    private static Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;
    @Value("${netty.work-group.size}")
    private int nettyWorkGroupSize;

    @Value("${netty.port}")
    private int nettyPort;
    //未完成连接的队列，三次握手未完成
    //已完成连接队列，三次握手完成，内核正等待进程执行的accept数量
    @Value("${netty.backlog}")
    private int backlog;

    private HttpServer() {
    }

    public static HttpServer getInstance() {
        return httpServer;
    }

    public void start() throws GatewayException {

        try {
            initEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(nettyPort))
                    //启动TCP_NODELAY，就意味着禁用了Nagle算法，允许小包的发送。对于延时敏感型，同时数据传输量比较小的应用，开启TCP_NODELAY选项无疑是一个正确的选择。比如，对于SSH会话，用户在远程敲击键盘发出指令的速度相对于网络带宽能力来说，绝对不是在一个量级上的，所以数据传输非常少；而又要求用户的输入能够及时获得返回，有较低的延时。如果开启了Nagle算法，就很可能出现频繁的延时，导致用户体验极差。当然，你也可以选择在应用层进行buffer，比如使用java中的buffered stream，尽可能地将大包写入到内核的写缓存进行发送；vectored I/O（writev接口）也是个不错的选择。
                    //
                    //
                    //对于关闭TCP_NODELAY，则是应用了Nagle算法。数据只有在写缓存中累积到一定量之后，才会被发送出去，这样明显提高了网络利用率（实际传输数据payload与协议头的比例大大提高）。但是这又不可避免地增加了延时；与TCP delayed ack这个特性结合，这个问题会更加显著，延时基本在40ms左右。当然这个问题只有在连续进行两次写操作的时候，才会暴露出来。
                    //
                    //连续进行多次对小数据包的写操作，然后进行读操作，本身就不是一个好的网络编程模式；在应用层就应该进行优化。
                    // 不使用nagle算法在压测的时候吞吐量相较要高一些
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_BACKLOG, backlog)
                    .childHandler(new ChannelHandlerInitializer());

            ChannelFuture channelFuture = bootstrap.bind(this.nettyPort).sync();
            logger.info("HttpServer name is " + HttpServer.class.getName() + " started and listen on " + channelFuture.channel().localAddress());
            //监听
            final ChannelFutureListener remover = new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    shutdown();
                }
            };
            channelFuture.channel().closeFuture().addListener(remover).sync();

        } catch (InterruptedException e) {
            logger.error("start http server error", e);
            shutdown();
        }


    }

    private void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    private void initEventLoopGroup() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup(nettyWorkGroupSize);
    }
}
