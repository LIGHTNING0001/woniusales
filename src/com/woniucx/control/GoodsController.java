package com.woniucx.control;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.upload.UploadFile;
import com.woniucx.core.CommonUtils;
import com.woniucx.core.JedisPoolUtils;
import com.woniucx.model.Goods;

import redis.clients.jedis.Jedis;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpSession;

public class GoodsController extends Controller {
	
	Jedis jedis;
	HttpSession session;
	
    public void index() {	
//        if (getSessionAttr("islogin") != "true") {
//            redirect("/");
//        } else if (!getSessionAttr("role").equals("admin")) {
//            renderHtml("<div style='font-size: 20px; color: red; margin: 100px auto; width: 500px; text-align: center;'>你不是管理员，无法进入批次导入.</div>");
//        } else {
//            setAttr("batchName", "GB" + CommonUtils.generateDateTime("yyyyMMdd"));
//            render("/page/goods.html");
//        } 
    	
    	jedis = JedisPoolUtils.getJedis();
		String sid = getSession().getId();

		String value = jedis.hget(sid, "islogin");
		String role  = jedis.hgetAll(sid).get("role");
		
		if(value == null || !value.equals("true")) {
			redirect("/");
		}else if(!role.equals("admin")) {
			renderHtml("<div style='font-size: 20px; color: red; margin: 100px auto; width: 500px; text-align: center;'>你不是管理员，无法进入批次导入.</div>");
		}else {
			 setAttr("batchName", "GB" + CommonUtils.generateDateTime("yyyyMMdd"));
			 render("/page/goods.html");
		}
    	
    	
    }
    
    public void upload() {
        UploadFile uploadFile = getFile("batchfile", "../upload");
        String str1 = getPara("batchname");
        List list1 = Goods.dao.find("select * from goods where batchname=?", new Object[] { str1 });
        if (list1.size() >= 1) {
            renderText("already-imported");
            return;
        } 
        String str2 = uploadFile.getUploadPath();
        String str3 = uploadFile.getFileName();
        String str4 = "GoodsList-" + str1 + ".xls";
        File file1 = new File(str2 + "/" + str3);
        File file2 = new File(str2 + "/" + str4);
        System.out.println("Current Batch File Path:  " + file2);
        file1.renameTo(file2);
        String str5 = str2 + "/" + str4;
        String[][] arrayOfString = CommonUtils.readExcel(str5);
        if (arrayOfString == null) {
            renderText("format-error");
            return;
        } 
        for (byte b = 0; b < arrayOfString.length; b++) {
            Goods goods = new Goods();
            goods.set("batchname", str1);
            goods.set("goodsserial", arrayOfString[b][0]);
            goods.set("goodsname", arrayOfString[b][1]);
            goods.set("quantity", arrayOfString[b][2]);
            goods.set("unitprice", arrayOfString[b][3]);
            goods.set("totalprice", arrayOfString[b][4]);
            goods.set("costunitprice", arrayOfString[b][5]);
            goods.set("costtotalprice", arrayOfString[b][6]);
            goods.set("userid", getSessionAttr("userid"));
            goods.set("createtime", CommonUtils.generateDateTime("yyyy-MM-dd HH:mm:ss"));
            goods.save();
        } 
        List list2 = Goods.dao.find("select * from goods where batchname=? order by goodsid", new Object[] { str1 });
        renderJson(list2);
    }
    
    public void querybatch() {
        String str = getPara("batchname");
        List list = Goods.dao.find("select * from goods where batchname=? order by goodsid", new Object[] { str });
        renderJson(list);
    }
    
    public void deletebatch() {
        String str1 = getPara("batchname");
        String str2 = "delete from goods where batchname=?";
        int i = Db.update(str2, new Object[] { str1 });
        if (i > 0) {
            renderText("delete-successful");
        } else {
            renderText("delete-failed");
        } 
    }
    
    public void querygoods() {
        int i = getParaToInt("goodsid").intValue();
        Goods goods = (Goods)Goods.dao.findById(Integer.valueOf(i));
        renderJson(goods);
    }
    
    public void editgoods() {
        String str1 = CommonUtils.generateDateTime("yyyy-MM-dd HH:mm:ss");
        int i = getParaToInt("goodsid").intValue();
        String str2 = getPara("goodsserial");
        String str3 = getPara("goodsname");
        int j = getParaToInt("quantity").intValue();
        float f1 = Float.parseFloat(getPara("unitprice"));
        float f2 = Float.parseFloat(getPara("totalprice"));
        float f3 = Float.parseFloat(getPara("costunitprice"));
        float f4 = Float.parseFloat(getPara("costtotalprice"));
        Goods goods = (Goods)Goods.dao.findById(Integer.valueOf(i));
        goods.set("goodsserial", str2);
        goods.set("goodsname", str3);
        goods.set("quantity", Integer.valueOf(j));
        goods.set("unitprice", Float.valueOf(f1));
        goods.set("totalprice", Float.valueOf(f2));
        goods.set("costunitprice", Float.valueOf(f3));
        goods.set("costtotalprice", Float.valueOf(f4));
        goods.set("createtime", str1);
        boolean bool = goods.update();
        if (bool) {
            renderText("edit-successful");
        } else {
            renderText("edit-failed");
        } 
    }
    
    public void deletegoods() {
        int i = getParaToInt("goodsid").intValue();
        boolean bool = ((Goods)Goods.dao.findById(Integer.valueOf(i))).delete();
        if (bool) {
            renderText("delete-successful");
        } else {
            renderText("delete-failed");
        } 
    }
}
