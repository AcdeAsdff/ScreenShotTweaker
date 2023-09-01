package com.linearity.screenshottweaker;

import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.Base64;

public class SM2EncryptUtils {

//    private static final String publicKey = "02cd77e007bdc86eeaf9a479ba7a2c22bc0a517ccb3a6975c3f94b4ac93347dea6";

    public static ECPublicKey hexToSM2PublicKey(String x, String y) {
        ECPublicKey pubkey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            ECPoint ecPoint = new ECPoint(
                    new BigInteger(x, 16), new BigInteger(y, 16));

            ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec("sm2p256v1");
            ECNamedCurveSpec spec = new ECNamedCurveSpec("sm2p256v1", parameterSpec.getCurve(), parameterSpec.getG(), parameterSpec.getN(), parameterSpec.getH(), parameterSpec.getSeed());

            ECPublicKeySpec keySpec = new ECPublicKeySpec(ecPoint, spec);
            pubkey = new BCECPublicKey("BC", keySpec, BouncyCastleProvider.CONFIGURATION);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return pubkey;
    }
//    from internet, don't use this key.
//    private static final String publicKey = "02cd77e007bdc86eeaf9a479ba7a2c22bc0a517ccb3a6975c3f94b4ac93347dea6".toLowerCase();
    private static Cipher cipher;
    public static KeyFactory keyFactory;

    static {
        try {
            BouncyCastleProvider provider = new BouncyCastleProvider();
            // 获取SM2相关参数
            X9ECParameters parameters = GMNamedCurves.getByName("sm2p256v1");
            // 椭圆曲线参数规格
            ECParameterSpec ecParameterSpec = new ECParameterSpec(parameters.getCurve(), parameters.getG(), parameters.getN(), parameters.getH());
            // 将公钥HEX字符串转换为椭圆曲线对应的点
//            ECPoint ecPoint = parameters.getCurve().decodePoint(Hex.decode(publicKey));
            // 获取椭圆曲线KEY生成器
            keyFactory = KeyFactory.getInstance("EC", provider);
            // 将椭圆曲线点转为公钥KEY对象
//            BCECPublicKey bcecPublicKey = (BCECPublicKey) keyFactory.generatePublic(new ECPublicKeySpec(ecPoint, ecParameterSpec));
            BCECPublicKey bcecPublicKey = (BCECPublicKey) hexToSM2PublicKey("cb2a400b23a144ddb8672eb66b5aba6ccdcc1d4f3d0823f3cc17fca8b52679d7",
                    "da85e6a829b787697f3573865fa5a8cc6fa7fa0eaf6988e53dd84efe1694900c");
            //i chose to forget private key so that it can hardly be cracked
            // 获取SM2加密器
            cipher = Cipher.getInstance("SM2", provider);
            // 初始化为加密模式
            cipher.init(Cipher.ENCRYPT_MODE, bcecPublicKey);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

//    public static void main(String[] args) {
//        System.out.print(encrypt("1145141919810"));
//    }

    /**
     * SM2 加密
     * @param content 需要加密的内容
     * @return 加密后的密文
     */
    public static String encrypt(String content) {
        try {
            String contentEncrypt = Base58Util.encode(cipher.doFinal(content.getBytes()));
//            when using base64, "/" will appear
//            String contentEncrypt = Base64.getEncoder().encodeToString(cipher.doFinal(content.getBytes()));
            return contentEncrypt;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}