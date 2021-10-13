package com.woniucx.control;

import com.jfinal.core.Controller;
import com.woniucx.core.CommonUtils;
import com.woniucx.core.JedisPoolUtils;
import com.woniucx.model.Customer;
import com.woniucx.model.Goods;
import com.woniucx.model.Return;
import com.woniucx.model.Sell;
import com.woniucx.model.SellSum;
import com.woniucx.model.StoreSum;

import redis.clients.jedis.Jedis;

import java.util.List;

import javax.servlet.http.HttpSession;

public class SellController extends Controller {
	
	Jedis jedis;
	HttpSession session;
	
    public void index() {
    	
    	jedis = JedisPoolUtils.getJedis();
    	String sid = getSession().getId();
    	
    	String value = jedis.hget(sid, "islogin");
    	
//        if (getSessionAttr("islogin") != "true") {
//            redirect("/");
//        } else {
//            render("/page/sell.html");
//        } 
    	
    	if(value != null && value.equals("true")) {
    		 render("/page/sell.html");
    	}else {
    		redirect("/");
    	}
    	
    }
    
    public void barcode() {
        String str1 = "select creditratio from sellsum order by sellsumid desc limit 0,1";
        String str2 = ((SellSum)SellSum.dao.findFirst(str1)).getStr("creditratio");
        String str3 = "select discountratio from sell order by sellid desc limit 0,1";
        String str4 = ((Sell)Sell.dao.findFirst(str3)).getStr("discountratio");
        System.out.println(str4);
        String str5 = getPara("barcode");
        List<Goods> list = Goods.dao.find("select barcode,goodsserial,goodsname,unitprice,createtime from goods where barcode=? order by goodsid desc limit 0,1", new Object[] { str5 });
        if (list.size() > 0) {
            List<StoreSum> list1 = StoreSum.dao.find("select * from storesum where barcode=?", new Object[] { str5 });
            String str = "";
            for (byte b = 0; b < list1.size(); b++) {
                String str6 = ((StoreSum)list1.get(b)).getStr("goodssize");
                int i = ((StoreSum)list1.get(b)).getInt("remained").intValue();
                str = str + "<option value='" + str6 + "'>尺码:" + str6 + ",剩余:" + i + "件</option>";
            } 
            ((Goods)list.get(0)).set("createtime", str + "##" + str2 + "##" + str4);
            renderJson(list);
        } else {
            renderJson("[]");
        } 
    }
    
    public void summary() {
        int k;
        if (getSessionAttr("islogin") != "true") {
            redirect("/");
            return;
        } 
        String str1 = CommonUtils.generateDateTime("yyyy-MM-dd HH:mm:ss");
        String str2 = getPara("customerphone");
        String str3 = getPara("paymethod");
        String str4 = getPara("totalprice");
        String str5 = getPara("creditratio");
        int i = getParaToInt("oldcredit").intValue();
        int j = getParaToInt("creditsum").intValue();
        String str6 = getPara("tickettype");
        String str7 = getPara("ticketsum");
        String str8 = "select customerid,creditcloth,credittotal from customer where customerphone=?";
        try {
            Customer customer = (Customer)Customer.dao.findFirst(str8, new Object[] { str2 });
            k = customer.getInt("customerid").intValue();
            int m = customer.getInt("credittotal").intValue() + j;
            int n = customer.getInt("creditcloth").intValue() + j;
            ((Customer)((Customer)((Customer)((Customer)Customer.dao.findById(Integer.valueOf(k))).set("credittotal", Integer.valueOf(m))).set("creditcloth", Integer.valueOf(n))).set("updatetime", str1)).update();
        } catch (Exception exception) {
            Customer customer = new Customer();
            customer.set("customerphone", str2);
            customer.set("customername", "未知");
            customer.set("childsex", "男");
            customer.set("childdate", CommonUtils.generateDateTime("yyyy-MM-dd"));
            customer.set("creditkids", Integer.valueOf(0));
            customer.set("creditcloth", Integer.valueOf(i + j));
            customer.set("credittotal", Integer.valueOf(i + j));
            customer.set("userid", getSessionAttr("userid"));
            customer.set("createtime", str1);
            customer.set("updatetime", str1);
            customer.save();
            k = customer.getInt("customerid").intValue();
            setSessionAttr("initcredit", Integer.valueOf(i));
        } 
        SellSum sellSum = new SellSum();
        sellSum.set("customerid", Integer.valueOf(k));
        sellSum.set("userid", getSessionAttr("userid"));
        sellSum.set("paymethod", str3);
        sellSum.set("totalprice", str4);
        sellSum.set("creditratio", str5);
        sellSum.set("creditsum", Integer.valueOf(j));
        sellSum.set("tickettype", str6);
        sellSum.set("ticketsum", str7);
        sellSum.set("createtime", str1);
        sellSum.save();
        String str9 = String.valueOf(sellSum.getInt("sellsumid"));
        renderText(str9);
    }
    
    public void detail() {
        String str1 = CommonUtils.generateDateTime("yyyy-MM-dd HH:mm:ss");
        String str2 = getPara("sellsumid");
        SellSum sellSum = (SellSum)SellSum.dao.findFirst("select customerid from sellsum where sellsumid=?", new Object[] { str2 });
        String str3 = sellSum.getStr("customerid");
        String str4 = getPara("barcode");
        String str5 = getPara("goodsserial");
        String str6 = getPara("goodsname");
        String str7 = getPara("goodssize");
        String str8 = getPara("unitprice");
        String str9 = getPara("discountratio");
        String str10 = getPara("discountprice");
        int i = getParaToInt("buyquantity").intValue();
        String str11 = getPara("subtotal");
        List<Goods> list = Goods.dao.find("select goodstype from goods where barcode=? and goodsserial=?", new Object[] { str4, str5 });
        String str12 = ((Goods)list.get(0)).getStr("goodstype");
        Sell sell = new Sell();
        sell.set("sellsumid", str2);
        sell.set("customerid", str3);
        sell.set("barcode", str4);
        sell.set("goodsserial", str5);
        sell.set("goodsname", str6);
        sell.set("goodstype", str12);
        sell.set("goodssize", str7);
        sell.set("unitprice", str8);
        sell.set("discountratio", str9);
        sell.set("discountprice", str10);
        sell.set("buyquantity", Integer.valueOf(i));
        sell.set("subtotal", str11);
        sell.set("createtime", str1);
        sell.save();
        String str13 = "select storesumid,quantity,remained from storesum where barcode=? and goodsserial=? and goodssize=?";
        StoreSum storeSum = (StoreSum)StoreSum.dao.findFirst(str13, new Object[] { str4, str5, str7 });
        int j = storeSum.getInt("storesumid").intValue();
        int k = storeSum.getInt("remained").intValue() - i;
        ((StoreSum)((StoreSum)((StoreSum)StoreSum.dao.findById(Integer.valueOf(j))).set("remained", Integer.valueOf(k))).set("updatetime", str1)).update();
        renderText("pay-successful");
    }
    
    public void prereturn() {
        int i = getParaToInt("sellid").intValue();
        Sell sell = (Sell)Sell.dao.findById(Integer.valueOf(i));
        float f = sell.getFloat("subtotal").floatValue();
        int j = sell.getInt("sellsumid").intValue();
        SellSum sellSum = (SellSum)SellSum.dao.findById(Integer.valueOf(j));
        int k = sellSum.getInt("totalprice").intValue();
        int m = sellSum.getInt("creditsum").intValue();
        int n = sellSum.getInt("ticketsum").intValue();
        int i1 = (int)(f / (k + n) * n);
        int i2 = (int)(f / (k + n) * m);
        int i3 = (int)f - i1;
        renderText(i2 + "#" + i1 + "#" + i3);
    }
    
    public void doreturn() {
        String str1 = CommonUtils.generateDateTime("yyyy-MM-dd HH:mm:ss");
        int i = getParaToInt("sellid").intValue();
        String str2 = getPara("goodsserial");
        Sell sell = (Sell)Sell.dao.findById(Integer.valueOf(i));
        if (sell == null) {
            renderText("goodsinfo-error");
            return;
        } 
        int j = sell.getInt("sellsumid").intValue();
        int k = sell.getInt("customerid").intValue();
        float f1 = sell.getFloat("subtotal").floatValue();
        String str3 = sell.getStr("barcode");
        String str4 = sell.getStr("goodsserial");
        String str5 = sell.getStr("goodsname");
        String str6 = sell.getStr("goodstype");
        String str7 = sell.getStr("goodssize");
        float f2 = sell.getFloat("unitprice").floatValue();
        float f3 = sell.getFloat("discountratio").floatValue();
        float f4 = sell.getFloat("discountprice").floatValue();
        int m = sell.getInt("buyquantity").intValue();
        String str8 = sell.getStr("createtime");
        if (!str4.equals(str2)) {
            renderText("goodsinfo-error");
            return;
        } 
        SellSum sellSum = (SellSum)SellSum.dao.findById(Integer.valueOf(j));
        int n = sellSum.getInt("totalprice").intValue();
        int i1 = sellSum.getInt("creditsum").intValue();
        int i2 = sellSum.getInt("ticketsum").intValue();
        int i3 = (int)(f1 / (n + i2) * i2);
        int i4 = (int)(f1 / (n + i2) * i1);
        int i5 = (int)f1 - i3;
        Return return_ = new Return();
        return_.set("sellid", Integer.valueOf(i));
        return_.set("sellsumid", Integer.valueOf(j));
        return_.set("customerid", Integer.valueOf(k));
        return_.set("barcode", str3);
        return_.set("goodsserial", str4);
        return_.set("goodsname", str5);
        return_.set("goodstype", str6);
        return_.set("goodssize", str7);
        return_.set("unitprice", Float.valueOf(f2));
        return_.set("discountratio", Float.valueOf(f3));
        return_.set("discountprice", Float.valueOf(f4));
        return_.set("buyquantity", Integer.valueOf(m));
        return_.set("subtotal", Float.valueOf(f1));
        return_.set("returnticket", Integer.valueOf(i3));
        return_.set("returncredit", Integer.valueOf(i4));
        return_.set("returnmoney", Integer.valueOf(i5));
        return_.set("selltime", str8);
        return_.set("createtime", str1);
        boolean bool = return_.save();
        if (bool) {
            boolean bool1 = Sell.dao.deleteById(Integer.valueOf(i));
            if (bool1) {
                String str = "select * from storesum where barcode=? and goodsserial=? and goodssize=?";
                StoreSum storeSum = (StoreSum)StoreSum.dao.findFirst(str, new Object[] { str3, str4, str7 });
                if (storeSum != null) {
                    int i8 = storeSum.getInt("remained").intValue();
                    ((StoreSum)((StoreSum)storeSum.set("remained", Integer.valueOf(i8 + m))).set("updatetime", str1)).update();
                } else {
                    renderText("goodsinfo-error");
                    return;
                } 
                sellSum.set("totalprice", Integer.valueOf(n - i5));
                sellSum.set("creditsum", Integer.valueOf(i1 - i4));
                sellSum.set("ticketsum", Integer.valueOf(i2 - i3));
                sellSum.update();
                Customer customer = (Customer)Customer.dao.findById(Integer.valueOf(k));
                int i6 = customer.getInt("creditcloth").intValue();
                int i7 = customer.getInt("credittotal").intValue();
                customer.set("creditcloth", Integer.valueOf(i6 - i4));
                customer.set("credittotal", Integer.valueOf(i7 - i4));
                customer.update();
            } 
            renderText("return-successful");
        } else {
            renderText("return-failed");
            return;
        } 
    }
}
