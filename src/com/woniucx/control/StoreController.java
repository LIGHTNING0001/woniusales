package com.woniucx.control;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.woniucx.core.CommonUtils;
import com.woniucx.core.JedisPoolUtils;
import com.woniucx.model.Goods;
import com.woniucx.model.Store;
import com.woniucx.model.StoreSum;

import redis.clients.jedis.Jedis;

import java.util.List;

import javax.servlet.http.HttpSession;

public class StoreController extends Controller {
	
	Jedis jedis;
	HttpSession session;
	
    public void index() {    
    	
//        if (getSessionAttr("islogin") != "true") {
//            redirect("/");
//        } else {
//            setAttr("batchname", getPara("batchname"));
//            setAttr("goodsserial", getPara("goodsserial"));
//            setAttr("goodsname", getPara("goodsname"));
//            setAttr("unitprice", getPara("unitprice"));
//            render("/page/store.html");
//        } 
    	
    	jedis = JedisPoolUtils.getJedis();
    	String sid = getSession().getId();
    	
    	String value = jedis.hget(sid, "islogin");
    	
    	if(value != null && value.equals("true")) {
    		 setAttr("batchname", getPara("batchname"));
    		 setAttr("goodsserial", getPara("goodsserial"));
    		 setAttr("goodsname", getPara("goodsname"));
    		 setAttr("unitprice", getPara("unitprice"));
    		 render("/page/store.html");
    	}else {
    		redirect("/");
    	}
    	
    }
    
    public void querybatch() {
        List list = Goods.dao.find("select distinct batchname from goods order by batchname desc");
        renderJson(list);
    }
    
    public void queryinfo() {
        String str1 = getPara("batchname");
        String str2 = getPara("goodsserial");
        List list = Goods.dao.find("select goodsname,unitprice,barcode,goodstype,inputsize,quantity from goods where goodsserial=?", new Object[] { str2 });
        renderJson(list);
    }
    
    public void queryserial() {
        String str1 = getPara("query");
        String str2 = getPara("batchname");
        List list = Goods.dao.find("select goodsserial from goods where goodsserial like '%" + str1 + "%' and batchname='" + str2 + "' limit 0,10");
        renderJson(list);
    }
    
    public void add() {
        String str1 = getPara("batchname");
        String str2 = getPara("goodsserial");
        String str3 = getPara("barcode");
        String str4 = getPara("inputsize");
        String str5 = getPara("goodstype");
        int i = getParaToInt("quantity").intValue();
        String str6 = CommonUtils.generateDateTime("yyyy-MM-dd HH:mm:ss");
        String[] arrayOfString = str4.split("-");
        int j = -1;
        try {
            String str = "select goodsid from goods where batchname=? and goodsserial=?";
            j = ((Goods)Goods.dao.findFirst(str, new Object[] { str1, str2 })).getInt("goodsid").intValue();
        } catch (NullPointerException nullPointerException) {
            renderText("失败：批次" + str1 + "下没有找到货号：" + str2 + ".");
            return;
        } 
        Goods goods = (Goods)Goods.dao.findById(Integer.valueOf(j));
        if (goods.getInt("quantity").intValue() != i * arrayOfString.length) {
            renderText("失败：入库的商品总量（数量*尺码个数）与本批次的商品总数量不一致.");
            return;
        } 
        if (!goods.getStr("barcode").equals("0")) {
            renderText("失败：该批次商品已经完成入库，请勿重复录入.");
            return;
        } 
        boolean bool = ((Goods)((Goods)((Goods)((Goods)Goods.dao.findById(Integer.valueOf(j))).set("barcode", str3)).set("goodstype", str5)).set("inputsize", str4)).update();
        if (bool) {
            for (byte b = 0; b < arrayOfString.length; b++) {
                Store store = new Store();
                store.set("goodsid", Integer.valueOf(j));
                store.set("goodssize", arrayOfString[b]);
                store.set("inputsize", str4);
                store.set("quantity", Integer.valueOf(i));
                store.set("userid", getSessionAttr("userid"));
                store.set("createtime", str6);
                store.save();
                String str7 = "select storesumid,quantity,remained from storesum where barcode=? and goodsserial=? and goodssize=?";
                List<StoreSum> list1 = StoreSum.dao.find(str7, new Object[] { str3, str2, arrayOfString[b] });
                if (list1.size() < 1) {
                    StoreSum storeSum = new StoreSum();
                    storeSum.set("barcode", str3);
                    storeSum.set("goodsserial", str2);
                    storeSum.set("goodssize", arrayOfString[b]);
                    storeSum.set("quantity", Integer.valueOf(i));
                    storeSum.set("remained", Integer.valueOf(i));
                    storeSum.set("createtime", str6);
                    storeSum.set("updatetime", str6);
                    storeSum.save();
                } else {
                    int k = ((StoreSum)list1.get(0)).getInt("storesumid").intValue();
                    int m = ((StoreSum)list1.get(0)).getInt("quantity").intValue();
                    int n = m + i;
                    int i1 = ((StoreSum)list1.get(0)).getInt("remained").intValue();
                    int i2 = i1 + i;
                    ((StoreSum)((StoreSum)((StoreSum)((StoreSum)StoreSum.dao.findById(Integer.valueOf(k))).set("quantity", Integer.valueOf(n))).set("remained", Integer.valueOf(i2))).set("updatetime", str6)).update();
                } 
            } 
            String str = "select g.goodsid, g.batchname, g.barcode, g.goodsserial, g.goodsname, s.inputsize, g.quantity, g.unitprice, u.realname from goods g, store s, user u where u.userid=s.userid and g.goodsid=s.goodsid and s.goodsid=? group by g.batchname and g.goodsserial";
            List<Record> list = Db.find(str, new Object[] { Integer.valueOf(j) });
            ((Record)list.get(0)).set("quantity", i + "手,共" + (arrayOfString.length * i) + "件");
            renderJson(list);
        } else {
            renderText("失败：更新条码信息失败，请联系管理员进行手工处理.");
        } 
    }
    
    public void edit() {
        int i = getParaToInt("storesumid").intValue();
        int j = getParaToInt("remained").intValue();
        StoreSum storeSum = (StoreSum)StoreSum.dao.findById(Integer.valueOf(i));
        storeSum.set("remained", Integer.valueOf(j));
        boolean bool = storeSum.update();
        if (bool) {
            renderText("edit-successful");
        } else {
            renderText("edit-failed");
        } 
    }
}
