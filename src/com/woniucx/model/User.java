package com.woniucx.model;

import com.jfinal.plugin.activerecord.Model;

public class User extends Model<User> {
    public static final User dao = (User)(new User()).dao();
}
