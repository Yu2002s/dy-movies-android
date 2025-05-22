package xyz.jdynb.dymovies.utils;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.util.Log;

import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

    private static final String XM_IV = "https://t.me/xmflv666";

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @Nullable
    public static String encrypt(String mode, String key, String content) {
        try {
            // 创建一个密钥规范
            SecretKeySpec secretKeySpec = new SecretKeySpec(hexStringToByteArray(key), "AES");
            // 创建一个Cipher实例，指定解密模式为AES/ECB/PKCS5Padding（PKCS5Padding兼容PKCS7Padding）
            @SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance(mode);
            // 初始化Cipher实例进行解密
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            // 执行解密
            byte[] decryptedBytes = cipher.doFinal(content.getBytes());
            return Base64.encodeToString(decryptedBytes, Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }

    public static String encrypt(String mode, String key, String iv, String content) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
        try {
            Cipher cipher = Cipher.getInstance(mode);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e("jdy", e.toString());
            return null;
        }
    }

    /**
     * 适配Node.js中AES-CBC-ZeroPadding加密
     *
     * @param sSrc           -- 待加密内容
     * @param encodingFormat -- 字符串编码方式
     * @param sKey           -- 加密密钥
     * @param ivParameter    -- 偏移量
     * @return Base64编码后的字符串，"":编码出错
     */
    public static String encryptCBCNoPadding(String sSrc, String encodingFormat, String sKey, String ivParameter) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(sKey.getBytes(encodingFormat), "AES"),
                    new IvParameterSpec(ivParameter.getBytes(encodingFormat)));
            int blockSize = cipher.getBlockSize();
            //用'\0'填充长度至整数倍,对应node.js中ZeroPadding
            StringBuilder dataBuilder = new StringBuilder(sSrc);
            if (sSrc.length() % blockSize != 0) {
                for (int i = 0; i < blockSize - (sSrc.length() % blockSize); i++) {
                    dataBuilder.append("\0");
                }
            }
            byte[] encrypted = cipher.doFinal(dataBuilder.toString().getBytes(encodingFormat));
            return Base64.encodeToString(encrypted, 0).trim();
        } catch (Exception e) {
            Log.e("jdy", e.toString());
            return "";
        }
    }

    /**
     * Xm flv aes 加密获取 key 值
     *
     * @param input 加密字符串
     * @return base64 key
     * @throws Exception 可能出现的问题
     */
    public static String sign(String input) throws Exception {
        // 1. 计算input的MD5值作为密钥
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] keyBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

        // 2. 准备IV，截取字符串的前16个字节
        byte[] ivBytes = XM_IV.getBytes(StandardCharsets.UTF_8);
        byte[] truncatedIv = new byte[16];
        System.arraycopy(ivBytes, 0, truncatedIv, 0, Math.min(ivBytes.length, 16));

        // 3. 创建加密所需的密钥和IV规范
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec iv = new IvParameterSpec(truncatedIv);

        // 4. 初始化加密器
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);

        int blockSize = cipher.getBlockSize();
        //用'\0'填充长度至整数倍,对应node.js中ZeroPadding
        StringBuilder dataBuilder = new StringBuilder(input);
        if (input.length() % blockSize != 0) {
            for (int i = 0; i < blockSize - (input.length() % blockSize); i++) {
                dataBuilder.append("\0");
            }
        }

        // 5. 执行加密
        byte[] encrypted = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));

        // 6. 转换为Base64字符串
        return Base64.encodeToString(encrypted, 0);
    }


    public static String decrypt(String mode, String key, String iv, String content) {
        try {
            byte[] bytes = Base64.decode(content, Base64.DEFAULT);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));//强烈注意：CBC必须要

            Cipher cipher = Cipher.getInstance(mode);// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);            // 初始化
            byte[] result = cipher.doFinal(bytes);
            return new String(result);
        } catch (Exception e) {
            Log.e("jdy", e.toString());
            return null;
        }
    }

}
