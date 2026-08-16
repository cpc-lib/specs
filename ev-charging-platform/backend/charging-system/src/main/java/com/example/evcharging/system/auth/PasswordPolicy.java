package com.example.evcharging.system.auth;

public final class PasswordPolicy {
    private PasswordPolicy(){}

    public static void validate(String password){
        if(password==null||password.length()<10)
            throw new IllegalArgumentException("password must be at least 10 characters");
        boolean letter=false,digit=false,other=false;
        for(char c:password.toCharArray()){
            if(Character.isLetter(c)) letter=true;
            else if(Character.isDigit(c)) digit=true;
            else other=true;
        }
        if(!(letter&&digit) && !(letter&&other) && !(digit&&other))
            throw new IllegalArgumentException("password must contain at least two character types");
    }
}
