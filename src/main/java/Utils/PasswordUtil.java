package Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordUtil {
    private static final String CHAR_POOL = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int SALT_LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成 10 位随机英文字符串作为密码盐值
     */
    public static String generateSalt() {
        StringBuilder sb = new StringBuilder(SALT_LENGTH);
        for (int i = 0; i < SALT_LENGTH; i++) {
            sb.append(CHAR_POOL.charAt(RANDOM.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }

    /**
     * 对 password + salt 进行 MD5 哈希，返回 32 位十六进制字符串
     */
    public static String md5Hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest((password + salt).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 算法不可用", e);
        }
    }

    /**
     * 校验密码：对输入密码加盐后 MD5，与存储密文比较
     */
    public static boolean verify(String inputPassword, String storedSalt, String storedHash) {
        String computed = md5Hash(inputPassword, storedSalt);
        return computed.equals(storedHash);
    }
}
