package com.woniucx.control;

import com.jfinal.core.Controller;
import com.woniucx.core.JedisPoolUtils;
import com.woniucx.model.User;

import redis.clients.jedis.Jedis;

import java.util.List;

import javax.servlet.http.HttpSession;

public class IndexController extends Controller {
	
	
	Jedis jedis;
	HttpSession session;
	
    public void index() {
    	
    	jedis = JedisPoolUtils.getJedis();
    	String sid = getSession().getId();
    	
    	String value = jedis.hget(sid, "islogin");
    	
        if (value == null || !value.equals("true")) {
            String str1 = getCookie("username");
            String str2 = getCookie("password");
            if (str1 != null && str1.length() > 0) {
                String str = "select * from user where username=? and password=?";
                List<User> list = User.dao.find(str, new Object[] { str1, str2 });
                if (list.size() == 1) {
                    setSessionAttr("islogin", "true");
                    setSessionAttr("userid", ((User)list.get(0)).getInt("userid"));
                    setSessionAttr("username", ((User)list.get(0)).getStr("username"));
                    setSessionAttr("realname", ((User)list.get(0)).getStr("realname"));
                    setSessionAttr("role", ((User)list.get(0)).getStr("role"));
                    render("/page/sell.html");
                } else {
                    render("/page/index.html");
                } 
            } else {
                render("/page/index.html");
            } 
        } else {
            render("/page/sell.html");
        } 
    }
    
    public void vcode() {
        renderCaptcha();
    }
    
    public void init() {
        renderText("");
    }
}
