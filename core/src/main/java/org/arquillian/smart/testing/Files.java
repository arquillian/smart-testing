package org.arquillian.smart.testing;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Files {

    private static MessageDigest messageDigest = null;

    static {
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private Files() {
    }

    /**
     * Computes the hash code using SHA-1 of given file
     * @param file
     * @return
     * @throws IOException
     */
    public static final String sha1(final File file) throws IOException {

        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            final byte[] buffer = new byte[1024];
            for (int read = 0; (read = is.read(buffer)) != -1;) {
                messageDigest.update(buffer, 0, read);
            }
        }

        return bytesToHex(messageDigest.digest());

    }

    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    /**
     * Transforms array of bytes to Hex format
     * @param bytes
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
