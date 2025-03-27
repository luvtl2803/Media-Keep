package com.anhq.mediakeep.utils.help;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

public class JwtUtils {
    public static boolean isJwtExpired(String jwt) {
        try {
            Log.d("JwtUtils", "Checking JWT: " + jwt);
            if (jwt == null || !jwt.contains(".")) {
                Log.e("JwtUtils", "Invalid JWT format");
                return true;
            }

            String[] parts = jwt.split("\\.");
            if (parts.length != 3) {
                Log.e("JwtUtils", "JWT does not have 3 parts");
                return true;
            }

            String payloadJson = new String(Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP), StandardCharsets.UTF_8);
            Log.d("JwtUtils", "Decoded JWT payload: " + payloadJson);

            JSONObject payload = new JSONObject(payloadJson);
            long exp = payload.optLong("exp", 0); // Lấy "exp", nếu không có thì mặc định là 0
            long now = System.currentTimeMillis() / 1000; // Thời gian hiện tại (giây)

            if (exp == 0) {
                Log.e("JwtUtils", "JWT has no expiration time");
                return true;
            }

            Log.d("JwtUtils", "JWT expires at: " + exp + ", current time: " + now);
            return now >= exp;
        } catch (Exception e) {
            Log.e("JwtUtils", "Error decoding JWT", e);
            return true; // Nếu lỗi, coi như JWT hết hạn
        }
    }
}
