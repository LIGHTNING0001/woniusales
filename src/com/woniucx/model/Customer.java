package com.woniucx.model;

import com.jfinal.plugin.activerecord.Model;

public class Customer extends Model<Customer> {
    public static final Customer dao = (Customer)(new Customer()).dao();
}
