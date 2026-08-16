import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

public class OpenApiHmacExample {
    public static void main(String[] args) throws Exception {
        String appKey=System.getenv().getOrDefault("OPEN_APP_KEY","evp_example");
        String appSecret=System.getenv().getOrDefault("OPEN_APP_SECRET","replace-me");
        String method="GET",path="/open-api/v1/stations",rawQuery="";
        byte[] body=new byte[0];
        String timestamp=String.valueOf(Instant.now().getEpochSecond());
        String nonce=UUID.randomUUID().toString();

        String canonical=method+"\n"+path+"\n"+rawQuery+"\n"+sha256(body)+"\n"+timestamp+"\n"+nonce;
        String signature=hmac(appSecret,canonical);

        System.out.println("X-App-Key: "+appKey);
        System.out.println("X-Timestamp: "+timestamp);
        System.out.println("X-Nonce: "+nonce);
        System.out.println("X-Signature-Version: v1");
        System.out.println("X-Signature: "+signature);
        System.out.println("X-Request-Id: "+UUID.randomUUID());
    }

    static String sha256(byte[] data)throws Exception{
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
    }
    static String hmac(String secret,String text)throws Exception{
        Mac mac=Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(text.getBytes(StandardCharsets.UTF_8)));
    }
}
