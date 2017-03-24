package org.restexpress.pipeline.factory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import org.restexpress.RestExpress;
import org.restexpress.intf.SpringInitCompleteAware;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslHandler;

/**
 * http server with ssl,args[0] 就是SSLContext
 * @author hanst
 *
 */
public class HttpServerSSLChannelHandlerFactory extends HttpServerChannelHandlerFactory implements SpringInitCompleteAware {
	private static SSLContext sslContext;
	
	@Override
	public void execute(){
		super.execute();
		try{
			KeyStore ks = KeyStore.getInstance("JKS");
			//keytool -genkey -keysize 2048 -validity 3650 -keyalg RSA -dname "CN=ait00.com" -keypass 654321 -storepass 123456 -keystore gornix.jks
			InputStream ksInputStream = new FileInputStream("/home/myyj/netty.keystore");
			ks.load(ksInputStream, "123456".toCharArray());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, "123456".toCharArray());
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), null, null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void build(ChannelPipeline pipeline,RestExpress restExpress,Object ...args) {
		//ssl channel handler要在下面的处理之前
		 
			SSLEngine sslEngine = sslContext.createSSLEngine();
			sslEngine.setUseClientMode(false);
			sslEngine.setNeedClientAuth(false);
			SslHandler sslHandler = new SslHandler(sslEngine);
			pipeline.addFirst("ssl", sslHandler); //必须是第一个处理
		 
		super.build(pipeline, restExpress);
	}
	
	@Override
	public String getDesc() {
		return "http server with ssl support";
	}
}
