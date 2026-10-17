package com.cloudticket.common.util;

import cn.hutool.crypto.digest.BCrypt;

//运用BCrypt加密,明文密码加盐得到加密hash秘钥
//秘钥加明文密码校验是否正确
public class PasswordUtil {

    private PasswordUtil(){

    }
    public static String hash(String password){
        return BCrypt.hashpw(password,BCrypt.gensalt());
    }

    public static boolean matches(String password,String hashCode){
        return BCrypt.checkpw(password,hashCode);
    }
}
