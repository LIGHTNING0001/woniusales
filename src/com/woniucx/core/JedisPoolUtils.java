package com.woniucx.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisPoolUtils {
	
	private static JedisPool jedisPool;
	 
	 static{
		 
		 InputStream is = JedisPoolUtils.class.getClassLoader().getResourceAsStream( "jedis.properties");
			//创建Properties对象
		 Properties pro = new Properties();
		 
		 try {
			pro.load(is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		 
		 poolConfig.setMaxTotal(Integer.parseInt(pro.getProperty("maxTotal")));
		 poolConfig.setMaxIdle(Integer.parseInt(pro.getProperty("maxIdle")));
		 
		 jedisPool = new JedisPool(poolConfig, pro.getProperty("host"), Integer.parseInt(pro.getProperty("port")));
	
	 }
	 
	 public static Jedis getJedis() {
		 return jedisPool.getResource();
	 }

}
