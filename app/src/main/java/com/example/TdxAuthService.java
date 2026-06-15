package com.example;

import android.util.Log;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;
import org.json.JSONObject;

import java.io.IOException;

/**
 * 處理交通部 TDX 授權流程，取得 Access Token
 */
public class TdxAuthService {
    private static final String TAG = "TdxAuthService";
    private static final String AUTH_URL = "https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token";

    // 儲存目前取得的 Token
    private static String cachedToken = null;
    private static long tokenExpiryTime = 0; // 毫秒戳記

    /**
     * 取得最新的 Access Token (如有快取且未過期，優先使用快取)
     * 必在非主執行緒(Background Thread)中呼叫
     */
    public static synchronized String getAccessToken(String clientId, String clientSecret) throws IOException {
        long currentTime = System.currentTimeMillis();
        // 提早 30 秒判定過期
        if (cachedToken != null && currentTime < tokenExpiryTime - 30000) {
            Log.d(TAG, "使用快取的 TDX Access Token");
            return cachedToken;
        }

        Log.d(TAG, "開始向 TDX 申請新的 Access Token...");
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .build();

        Request request = new Request.Builder()
                .url(AUTH_URL)
                .post(formBody)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                Log.e(TAG, "TDX 驗證失敗 Code: " + response.code() + ", 錯誤訊息: " + errorBody);
                throw new IOException("TDX 驗證失敗: HTTP " + response.code());
            }

            String responseData = response.body().string();
            JSONObject jsonObject = new JSONObject(responseData);
            String token = jsonObject.getString("access_token");
            long expiresInSeconds = jsonObject.optLong("expires_in", 3600); // 預設 1 小時

            cachedToken = token;
            tokenExpiryTime = System.currentTimeMillis() + (expiresInSeconds * 1000);
            Log.i(TAG, "成功取得 TDX Access Token，有效秒數: " + expiresInSeconds);
            return token;
        } catch (Exception e) {
            Log.e(TAG, "TDX 認證過程中發生錯誤", e);
            throw new IOException("TDX 認證失敗: " + e.getMessage());
        }
    }
}
