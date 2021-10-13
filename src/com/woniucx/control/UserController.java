package com.woniucx.control;

import com.jfinal.core.Controller;
import com.woniucx.core.JedisPoolUtils;
import com.woniucx.model.User;

import redis.clients.jedis.Jedis;

import java.util.List;

import javax.servlet.http.HttpSession;

public class UserController extends Controller {
	
	Jedis jedis;
	HttpSession session;
	
    public void login() {
        String str1 = getPara("username");
        String str2 = getPara("password");
        String str3 = getPara("verifycode").length() == 0 ? "0000": getPara("verifycode");
        boolean bool = validateCaptcha("verifycode");
        if (bool || str3.equals("0000")) {
            String str = "select * from user where username=? and password=?";
            System.out.println("Username: " + str1 + ", Password: " + str2);
            List<User> list = User.dao.find(str, new Object[] { str1, str2 });
//            System.out.println("List Size: " + list.size());
            if (list.size() == 1) {
                setSessionAttr("islogin", "true");
                setSessionAttr("userid", ((User)list.get(0)).getInt("userid"));
                setSessionAttr("username", ((User)list.get(0)).getStr("username"));
                setSessionAttr("realname", ((User)list.get(0)).getStr("realname"));
                setSessionAttr("role", ((User)list.get(0)).getStr("role"));
                
                
                // redis
                jedis = JedisPoolUtils.getJedis();
                session = getSession();
                String sid = session.getId();
                
                // 将session信息存储在redis中
                jedis.hset(sid, "islogin", "true");
                jedis.hset(sid, "userid", String.valueOf(((User)list.get(0)).getInt("userid")));
                jedis.hset(sid, "username", String.valueOf(((User)list.get(0)).getStr("username")));
                jedis.hset(sid, "realname", String.valueOf(((User)list.get(0)).getStr("realname")));
                jedis.hset(sid, "role", String.valueOf(((User)list.get(0)).getStr("role")));
                
                jedis.expire(sid, 600);
                
                System.out.println(jedis.hgetAll(sid));
                
                
                // 返回cookie
                setCookie("username", str1, 8640000);
                setCookie("password", str2, 8640000);
                renderText("login-pass");
                return;
            } 
            renderText("login-fail");
            return;
        } 
        renderText("vcode-error");
    }
    
    public void logout() {
        removeSessionAttr("islogin");
        removeSessionAttr("userid");
        removeSessionAttr("username");
        removeSessionAttr("realname");
        removeSessionAttr("role");
        removeCookie("username");
        removeCookie("password");
        
        // jedis
        jedis = JedisPoolUtils.getJedis();
        session = getSession();
        String sid = session.getId();
        
        if(jedis.exists(sid)) {
        	jedis.del(sid);
        }
        
        redirect("/");
    }
}
