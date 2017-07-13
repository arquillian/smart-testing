package org.arquillian.smart.testing.surefire.provider;

public class Validate {

    public static boolean isEmpty(String string){
        return string == null || string.trim().isEmpty();
    }

    public static boolean isNotEmpty(String string){
        return !isEmpty(string);
    }

    public static boolean isDisableSmartTesting() {
        String disableSmartTesting = System.getProperty("disableSmartTesting");
        if (disableSmartTesting != null && disableSmartTesting.equals("true")){
            return true;
        }
        return false;
    }
}
