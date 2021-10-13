package com.woniucx.control;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.woniucx.core.CommonUtils;
import com.woniucx.core.JedisPoolUtils;
import com.woniucx.model.Customer;

import redis.clients.jedis.Jedis;

import java.util.List;

import javax.servlet.http.HttpSession;

public class CustomerController extends Controller {

	Jedis jedis;
	HttpSession session;

	public void index() {

		jedis = JedisPoolUtils.getJedis();
		String sid = getSession().getId();

		String value = jedis.hget(sid, "islogin");
		
		if (value != null && value.equals("true")) {
			render("/page/customer.html");
		} else {
			redirect("/");
		}
	}

	public void query() {
		String str = getPara("customerphone");
		if (str != "") {
			List list = Customer.dao.find("select * from customer where customerphone=?", new Object[] { str });
			renderJson(list);
			return;
		}
	}

	public void phone() {
		String str = getPara("query");
		List list = Customer.dao
				.find("select customerphone from customer where customerphone like '%" + str + "%' limit 0,10");
		renderJson(list);
	}

	public void add() {
		String str1 = getPara("customername");
		String str2 = getPara("customerphone");
		String str3 = getPara("childsex");
		String str4 = getPara("childdate");
		int i = getParaToInt("creditkids").intValue();
		int j = getParaToInt("creditcloth").intValue();
		if (str1.length() < 1)
			str1 = "未知";
		if (str4.length() < 1)
			str4 = "0000-00-00";
		List list = Customer.dao.find("select customerid from customer where customerphone=?", new Object[] { str2 });
		if (list.size() >= 1) {
			renderText("already-added");
			return;
		}
		String str5 = CommonUtils.generateDateTime("yyyy-MM-dd HH:mm:ss");
		Customer customer = new Customer();
		customer.set("customername", str1);
		customer.set("customerphone", str2);
		customer.set("childsex", str3);
		customer.set("childdate", str4);
		customer.set("creditkids", Integer.valueOf(i));
		customer.set("creditcloth", Integer.valueOf(j));
		customer.set("credittotal", Integer.valueOf(i + j));
		customer.set("userid", getSessionAttr("userid"));
		customer.set("createtime", str5);
		customer.set("updatetime", str5);
		boolean bool = customer.save();
		if (bool) {
			renderText("add-successful");
			return;
		}
		renderText("add-failed");
	}

	public void search() {
		String str1 = getPara("customerphone");
		int i = (getParaToInt("page").intValue() - 1) * 30;
		String str2 = "select c.*, sum(s.totalprice) totals,count(s.customerid) counts from customer c left join sellsum s on c.customerid=s.customerid where c.customerphone like '%"
				+ str1 + "%' group by c.customerid limit " + i + ",30";
		List list = Db.find(str2);
		renderJson(list);
	}

	public void edit() {
		String str1 = getPara("customerid");
		String str2 = getPara("customerphone");
		String str3 = getPara("customername");
		String str4 = getPara("childsex");
		String str5 = getPara("childdate");
		int i = getParaToInt("creditkids").intValue();
		int j = getParaToInt("creditcloth").intValue();
		int k = i + j;
		boolean bool = ((Customer) ((Customer) ((Customer) ((Customer) ((Customer) ((Customer) ((Customer) ((Customer) Customer.dao
				.findById(str1)).set("customerphone", str2)).set("customername", str3)).set("childsex", str4))
						.set("childdate", str5)).set("creditkids", Integer.valueOf(i))).set("creditcloth",
								Integer.valueOf(j))).set("credittotal", Integer.valueOf(k))).update();
		if (bool) {
			renderText("edit-successful");
		} else {
			renderText("edit-failed");
		}
	}
}
