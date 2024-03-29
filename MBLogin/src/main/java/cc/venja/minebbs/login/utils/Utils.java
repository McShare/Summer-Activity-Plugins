package cc.venja.minebbs.login.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class Utils {

    private static final String MD5_ALGORITHM_NAME = "MD5";
    private static final char[] HEX_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String md5DigestAsHex(byte[] bytes) {
        return digestAsHexString(MD5_ALGORITHM_NAME, bytes);
    }

    private static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException var2) {
            throw new IllegalStateException("Could not find MessageDigest with algorithm \"" + algorithm + "\"", var2);
        }
    }

    private static byte[] digest(String algorithm, byte[] bytes) {
        return getDigest(algorithm).digest(bytes);
    }

    private static String digestAsHexString(String algorithm, byte[] bytes) {
        var hexDigest = digestAsHexChars(algorithm, bytes);
        return new String(hexDigest);
    }

    private static char[] digestAsHexChars(String algorithm, byte[] bytes) {
        var digest = digest(algorithm, bytes);
        return encodeHex(digest);
    }

    private static char[] encodeHex(byte[] bytes) {
        var chars = new char[32];

        for(int i = 0; i < chars.length; i += 2) {
            var b = bytes[i / 2];
            chars[i] = HEX_CHARS[b >>> 4 & 15];
            chars[i + 1] = HEX_CHARS[b & 15];
        }

        return chars;
    }

}
