package com.lmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by Xyg on 2020/4/14.
 */
public class Const {
    public static final String CURRENT_USER = "currentUser";
    public static final String Email = "email";
    public static final String USERNAME = "username";

    public interface ProductListOderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }

    public interface Role{

        int ROLE_CUSTOMER = 0; //普通用户
        int ROLE_ADMIN = 1; //管理员

    }

    public enum  ProductStatusEnum{
        ON_SALE(1,"在线");

        private  String value;
        private  int code;
        ProductStatusEnum(int code,String value){
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }
}
