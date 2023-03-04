package com.jaeger.findviewbyme.util;

/**
 * Created by Jaeger on 2016/12/31.
 * <p>
 * Email: chjie.jaeger@gamil.com
 * GitHub: https://github.com/laobie
 */
public class TextUtils {
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static boolean equals(CharSequence a, CharSequence b) {
        if (a == b) return true;
        int length;
        if (a != null && b != null && (length = a.length()) == b.length()) {
            if (a instanceof String && b instanceof String) {
                return a.equals(b);
            } else {
                for (int i = 0; i < length; i++) {
                    if (a.charAt(i) != b.charAt(i)) return false;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 去除首尾的换行符
     */
    public static String trimLines(String str) {
        if (isEmpty(str)) return str;
        int len = str.length();
        int st = 0;

        while ((st < len) && ((str.charAt(st) == '\n' || str.charAt(st) == ' '))) {
            st++;
        }
        while ((st < len) && ((str.charAt(len - 1) == '\n' || str.charAt(len - 1) == ' '))) {
            len--;
        }
        return ((st > 0) || (len < str.length())) ? str.substring(st, len) : str;
    }
}
