package com.woniucx.control;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.woniucx.core.CommonUtils;
import com.woniucx.core.JedisPoolUtils;
import com.woniucx.model.Customer;
import com.woniucx.model.Sell;
import com.woniucx.model.SellSum;

import redis.clients.jedis.Jedis;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpSession;

public class ReportController extends Controller {
	
	Jedis jedis;
	HttpSession session;
	
    public void index() {
    	
    	jedis = JedisPoolUtils.getJedis();
    	String sid = getSession().getId();
    	
    	String value = jedis.hget(sid, "islogin");
//        if (getSessionAttr("islogin") != "true") {
//            redirect("/");
//        } else {
//            render("/page/report.html");
//        } 
    	
    	if(value != null && value.equals("true")) {
    		render("/page/report.html");
    	}else {
    		 redirect("/");
    	}
    }
    
    public void totalselltoday() {
        String str1 = CommonUtils.generateDateTime("yyyy-MM-dd 00:00:00");
        String str2 = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
        String str3 = "select sum(totalprice) amount from sellsum where createtime>='" + str1 + "' and createtime <='" + str2 + "'";
        String str4 = ((SellSum)SellSum.dao.findFirst(str3)).getStr("amount");
        renderText(str4 + " 元");
    }
    
    public void totalsellweek() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(7, -7);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        String str1 = simpleDateFormat.format(calendar.getTime());
        String str2 = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
        String str3 = "select sum(totalprice) amount from sellsum where createtime>='" + str1 + "' and createtime <='" + str2 + "'";
        String str4 = ((SellSum)SellSum.dao.findFirst(str3)).getStr("amount");
        renderText(str4 + " 元");
    }
    
    public void totalsellmonth() {
        String str1 = CommonUtils.generateDateTime("yyyy-MM-01 00:00:00");
        String str2 = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
        String str3 = "select sum(totalprice) amount from sellsum where createtime>='" + str1 + "' and createtime <='" + str2 + "'";
        String str4 = ((SellSum)SellSum.dao.findFirst(str3)).getStr("amount");
        renderText(str4 + " 元");
    }
    
    public void totalsellyear() {
        String str1 = CommonUtils.generateDateTime("yyyy-01-01 00:00:00");
        String str2 = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
        String str3 = "select sum(totalprice) amount from sellsum where createtime>='" + str1 + "' and createtime <='" + str2 + "'";
        String str4 = ((SellSum)SellSum.dao.findFirst(str3)).getStr("amount");
        renderText(str4 + " 元");
    }
    
    public void totalsellall() {
        String str1 = "select sum(totalprice) amount from sellsum";
        String str2 = ((SellSum)SellSum.dao.findFirst(str1)).getStr("amount");
        renderText(str2 + " 元");
    }
    
    public void customercount() {
        String str1 = "select count(*) counts from customer";
        String str2 = ((Customer)Customer.dao.findFirst(str1)).getStr("counts");
        renderText(str2 + " 人");
    }
    
    public void customerbuyonce() {
        String str = "select count(customerid) from sellsum group by customerid having count(customerid)=1";
        List list = SellSum.dao.find(str);
        renderText(String.valueOf(list.size()) + " 人");
    }
    
    public void customerbuytwice() {
        String str = "select count(customerid) from sellsum group by customerid having count(customerid)=2";
        List list = SellSum.dao.find(str);
        renderText(String.valueOf(list.size()) + " 人");
    }
    
    public void customerbuymany() {
        String str = "select count(customerid) from sellsum group by customerid having count(customerid)>2";
        List list = SellSum.dao.find(str);
        renderText(String.valueOf(list.size()) + " 人");
    }
    
    public void myselltoday() {
        String str1 = CommonUtils.generateDateTime("yyyy-MM-dd 00:00:00");
        String str2 = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
        String str3 = "select sum(totalprice) amount from sellsum where userid=? and createtime>='" + str1 + "' and createtime <='" + str2 + "'";
        String str4 = ((SellSum)SellSum.dao.findFirst(str3, new Object[] { getSessionAttr("userid").toString() })).getStr("amount");
        renderText(str4 + " 元");
    }
    
    public void mysellmonth() {
        String str1 = CommonUtils.generateDateTime("yyyy-MM-01 00:00:00");
        String str2 = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
        String str3 = "select sum(totalprice) amount from sellsum where userid=? and createtime>='" + str1 + "' and createtime <='" + str2 + "'";
        String str4 = ((SellSum)SellSum.dao.findFirst(str3, new Object[] { getSessionAttr("userid").toString() })).getStr("amount");
        renderText(str4 + " 元");
    }
    
    public void selldetailtoday() {
        String str1 = CommonUtils.generateDateTime("yyyy-MM-dd 00:00:00");
        String str2 = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
        String str3 = "select s.*,c.customerphone from sell s, customer c where s.customerid=c.customerid and s.createtime>='" + str1 + "' and s.createtime <='" + str2 + "' order by s.sellid desc";
        List list = Sell.dao.find(str3);
        renderJson(list);
    }
    
    public void selldetailmonth() {
        String str1 = CommonUtils.generateDateTime("yyyy-MM-01 00:00:00");
        String str2 = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
        String str3 = "select s.*,c.customerphone from sell s, customer c where s.customerid=c.customerid and s.createtime>='" + str1 + "' and s.createtime <='" + str2 + "' order by s.sellid desc";
        List list = Sell.dao.find(str3);
        renderJson(list);
    }
    
    public void selldetailall() {
        int i = (getParaToInt("page").intValue() - 1) * 50;
        String str = "select s.*,c.customerphone from sell s, customer c where s.customerid=c.customerid order by s.sellid desc limit " + i + ",50";
        List list = Sell.dao.find(str);
        renderJson(list);
    }
    
    public void sellsumtoday() {
        String str1 = CommonUtils.generateDateTime("yyyy-MM-dd 00:00:00");
        String str2 = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
        String str3 = "select s.*, c.customerphone, u.realname from sellsum s, customer c, user u where s.customerid=c.customerid and s.userid=u.userid and s.createtime>='" + str1 + "' and s.createtime<='" + str2 + "' order by s.sellsumid desc";
        List list = Db.find(str3);
        renderJson(list);
    }
    
    public void sellsummonth() {
        String str1 = CommonUtils.generateDateTime("yyyy-MM-01 00:00:00");
        String str2 = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
        String str3 = "select s.*, c.customerphone, u.realname from sellsum s, customer c, user u where s.customerid=c.customerid and s.userid=u.userid and s.createtime>='" + str1 + "' and s.createtime<='" + str2 + "' order by s.sellsumid desc";
        List list = Db.find(str3);
        renderJson(list);
    }
    
    public void sellsumall() {
        int i = (getParaToInt("page").intValue() - 1) * 50;
        String str = "select s.*, c.customerphone, u.realname from sellsum s, customer c, user u where s.customerid=c.customerid and s.userid=u.userid order by s.sellsumid desc limit " + i + ",50";
        List list = Db.find(str);
        renderJson(list);
    }
    
    public void selltypemonth() {
        String str1 = CommonUtils.generateDateTime("yyyy-MM-01 00:00:00");
        String str2 = CommonUtils.generateDateTime("yyyy-MM-dd 23:59:59");
        String str3 = "select goodstype,sum(buyquantity) buyquantity,floor(sum(subtotal)) selltotal from sell where createtime>='" + str1 + "' and createtime<='" + str2 + "' group by goodstype";
        List list = Sell.dao.find(str3);
        renderJson(list);
    }
    
    public void selltypeall() {
        String str = "select goodstype,sum(buyquantity) buyquantity,floor(sum(subtotal)) selltotal from sell group by goodstype";
        List list = Sell.dao.find(str);
        renderJson(list);
    }
    
    public void returndetail() {
        int i = (getParaToInt("page").intValue() - 1) * 30;
        String str = "select r.*, c.customerphone from `return` r left join customer c on r.customerid=c.customerid limit " + i + ",30";
        List list = Db.find(str);
        renderJson(list);
    }
}
