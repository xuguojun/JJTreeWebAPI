/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jjtree.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author rose
 */
public class JString {

    private static final String EMAIL_PATTERN
            = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final String PHONE_PATTERN = "[0-9]{11}";

    public static boolean isPhoneNumber(String text) {
        Pattern p = Pattern.compile(PHONE_PATTERN);
        Matcher m = p.matcher(text);
        boolean matchFound = m.matches();
        return matchFound;
    }

    public static boolean isEmail(String text) {

        Pattern p = Pattern.compile(EMAIL_PATTERN);
        Matcher m = p.matcher(text);
        boolean matchFound = m.matches();
        return matchFound;
    }
}
