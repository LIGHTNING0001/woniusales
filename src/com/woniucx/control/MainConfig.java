package com.woniucx.control;

import com.jfinal.aop.Interceptor;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.ext.handler.ContextPathHandler;
import com.jfinal.ext.interceptor.SessionInViewInterceptor;
import com.jfinal.handler.Handler;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.IPlugin;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.IDataSourceProvider;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.template.Engine;
import com.woniucx.model.Customer;
import com.woniucx.model.Goods;
import com.woniucx.model.Return;
import com.woniucx.model.Sell;
import com.woniucx.model.SellSum;
import com.woniucx.model.Store;
import com.woniucx.model.StoreSum;
import com.woniucx.model.User;

public class MainConfig extends JFinalConfig {
    public void configConstant(Constants paramConstants) {
        PropKit.use("db.properties");
        paramConstants.setDevMode(true);
    }
    
    public void configEngine(Engine paramEngine) {}
    
    public void configHandler(Handlers paramHandlers) {
        paramHandlers.add((Handler)new ContextPathHandler("basePath"));
    }
    
    public void configInterceptor(Interceptors paramInterceptors) {
        paramInterceptors.add((Interceptor)new SessionInViewInterceptor());
    }
    
    public void configPlugin(Plugins paramPlugins) {
        DruidPlugin druidPlugin = new DruidPlugin(PropKit.get("db_url"), PropKit.get("db_username"), PropKit.get("db_password"));
        paramPlugins.add((IPlugin)druidPlugin);
        ActiveRecordPlugin activeRecordPlugin = new ActiveRecordPlugin((IDataSourceProvider)druidPlugin);
        paramPlugins.add((IPlugin)activeRecordPlugin);
        activeRecordPlugin.addMapping("user", "userid", User.class);
        activeRecordPlugin.addMapping("goods", "goodsid", Goods.class);
        activeRecordPlugin.addMapping("customer", "customerid", Customer.class);
        activeRecordPlugin.addMapping("store", "storeid", Store.class);
        activeRecordPlugin.addMapping("storesum", "storesumid", StoreSum.class);
        activeRecordPlugin.addMapping("sell", "sellid", Sell.class);
        activeRecordPlugin.addMapping("sellsum", "sellsumid", SellSum.class);
        activeRecordPlugin.addMapping("return", "returnid", Return.class);
    }
    
    public void configRoute(Routes paramRoutes) {
        paramRoutes.setBaseViewPath("/");
        paramRoutes.add("/", IndexController.class);
        paramRoutes.add("/user", UserController.class);
        paramRoutes.add("/sell", SellController.class);
        paramRoutes.add("/store", StoreController.class);
        paramRoutes.add("/goods", GoodsController.class);
        paramRoutes.add("/customer", CustomerController.class);
        paramRoutes.add("/query", QueryController.class);
        paramRoutes.add("/report", ReportController.class);
    }
}
