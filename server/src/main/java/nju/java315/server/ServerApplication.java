package nju.java315.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

@SpringBootApplication
public class ServerApplication {

	static private final Logger LOGGER = LoggerFactory.getLogger(ServerApplication.class);

	public static void main(String[] args) {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workGroup = new NioEventLoopGroup();

		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workGroup);
		b.channel(NioServerSocketChannel.class);
		b.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {

				ch.pipeline().addLast(
					new HttpServerCodec(), 
					new HttpObjectAggregator(65535),
					new WebSocketServerProtocolHandler("/websocket"),
					new GameMSgHandler()
				);
			}
		});

		b.option(ChannelOption.SO_BACKLOG, 128);
		b.childOption(ChannelOption.SO_KEEPALIVE, true);

		try {
			// 绑定端口
			ChannelFuture f = b.bind(Integer.parseInt(args[1])).sync();

			if (f.isSuccess()){
				LOGGER.info("Server start successfully!");
			}

			// 等待服务器信道关闭
			f.channel().closeFuture().sync();

		} catch (Exception e){
			workGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
			LOGGER.error(e.getMessage(), e);
		}
	}

}