package com.woniucx.model;

import com.jfinal.plugin.activerecord.Model;

public class Store extends Model<Store> {
    public static final Store dao = (Store)(new Store()).dao();
}
