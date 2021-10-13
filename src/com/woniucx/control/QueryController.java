package com.woniucx.control;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.woniucx.core.JedisPoolUtils;
import com.woniucx.model.Goods;

import redis.clients.jedis.Jedis;

import java.util.List;

import javax.servlet.http.HttpSession;

public class QueryController extends Controller {
	
	
	Jedis jedis;
	HttpSession session;
	
    public void index() {
    	
    	jedis = JedisPoolUtils.getJedis();
    	String sid = getSession().getId();
    		
    	String value = jedis.hget(sid, "islogin");
    	
//        if (getSessionAttr("islogin") != "true") {
//            redirect("/");
//        } else {
//            render("/page/query.html");
//        } 
    	
    	if(value != null && value.equals("true")) {
    		render("/page/query.html");
    	}else {
    		 redirect("/");
    	}
    	
    }
    
    public void stored() {
        String str1 = getPara("goodsserial");
        String str2 = getPara("goodsname");
        if(str2 == null) str2 = "";
        String str3 = getPara("barcode");
        if(str3 == null) str3 = "";
        String str4 = getPara("goodstype");
        if(str4 == null) str4 = "";
        String str5 = getPara("earlystoretime");
        if(str5 == null) str5 = "";
        String str6 = getPara("laststoretime");
        if(str6 == null) str6 = "";
//        int i = (getParaToInt("page").intValue() - 1) * 50;
        int i = getParaToInt("page") == null ? 0 : (getParaToInt("page").intValue() - 1) * 50 ;
        
        String str7 = "select s.storesumid, s.goodsserial, s.barcode, g.goodsname, s.goodssize, g.unitprice, s.quantity, s.remained, s.createtime from storesum s, goods g where s.barcode=g.barcode ";
        if (str1.length() >= 1)
            str7 = str7 + "and s.goodsserial='" + str1 + "' "; 
        if (str2.length() >= 1)
            str7 = str7 + "and g.goodsname like '%" + str2 + "%' "; 
        if (str3.length() >= 1)
            str7 = str7 + "and s.barcode='" + str3 + "' "; 
        if (str4.length() >= 1)
            str7 = str7 + "and g.goodstype='" + str4 + "' "; 
        if (str5.length() >= 1 && str6.length() < 1)
            str7 = str7 + "and s.createtime>='" + str5 + " 00:00:00' "; 
        if (str5.length() < 1 && str6.length() >= 1)
            str7 = str7 + "and s.createtime<='" + str6 + " 23:59:59' "; 
        if (str5.length() >= 1 && str6.length() >= 1)
            str7 = str7 + "and s.createtime>='" + str5 + " 00:00:00' and s.createtime<='" + str6 + " 23:59:59' "; 
        str7 = str7 + "order by storesumid limit " + i + ",50";
        List list = Db.find(str7);
        renderJson(list);
    }
    
    public void notstored() {
        int i = (getParaToInt("page").intValue() - 1) * 50;
        String str = "select goodsid,batchname,goodsserial,goodsname,unitprice,quantity,createtime from goods where barcode='0' limit " + i + ",50";
        List list = Goods.dao.find(str);
        renderJson(list);
    }
    
    public void zerostored() {
        int i = (getParaToInt("page").intValue() - 1) * 50;
        String str = "select s.storesumid, s.goodsserial, s.barcode, g.goodsname, s.goodssize, g.unitprice, s.quantity, s.remained, s.createtime from storesum s, goods g where s.barcode=g.barcode and s.remained=0 limit " + i + ",50";
        List list = Db.find(str);
        renderJson(list);
    }
}
