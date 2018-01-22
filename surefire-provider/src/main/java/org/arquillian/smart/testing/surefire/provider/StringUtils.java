package org.arquillian.smart.testing.surefire.provider;

/*
    This code copies from Apache commons-lang,
    under the Apache License 2.0 (see: https://git.io/vN2bs)
*/

public class StringUtils {

    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        } else {
            int sz = str.length();

            for(int i = 0; i < sz; ++i) {
                if (!Character.isDigit(str.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }

}
