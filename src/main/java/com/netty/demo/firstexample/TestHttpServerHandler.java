package com.netty.demo.firstexample;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import java.net.URI;

/**
 * 继承InboundHandler类，代表处理进入的请求，还有OutboundHandler,处理出去请求
 * 其中里面的泛型表示msg的类型，如果指定了HttpObject，表明这是个HTTP连接的对象
 */
public class TestHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    //channelRead0读取客户端请求，并返回响应的方法
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        //如果不加这个判断使用curl 测试会报错，使用curl测试命令curl "http://localhost:8899"
        //判断这个是不是httprequest请求
        if (msg instanceof HttpRequest) {
            System.out.println(msg.getClass());
            System.out.println(ctx.channel().remoteAddress());
            HttpRequest request = (HttpRequest) msg;
            URI uri = new URI(request.uri());
            //判断url是否请求了favicon.ico
            if ("/favicon.ico".equals(uri.getPath())) {
                System.out.println("请求了favicon.ico");
                return;
            }
            /**
             * 上面这段代码是验证如果用浏览器访问
             * chrome浏览器发起了两次请求，一次是发起的端口，第二次是请求/favicon.ico图标
             * 具体可以查看chrome的请求
             */

            System.out.println("请求方法名：" + request.method().name());
            //ByteBuf,neety中极为重要的概念，代表响应返回的数据
            ByteBuf content = Unpooled.copiedBuffer("Hello World", CharsetUtil.UTF_8);
            //构造一个http响应,HttpVersion.HTTP_1_1:采用http1.1协议，HttpResponseStatus.OK：状态码200
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

            //如果只是调用write方法，他仅仅是存在缓冲区里，并不会返回客户端
            //调用writeAndFlush可以
            ctx.writeAndFlush(response);

        }

    }
}
