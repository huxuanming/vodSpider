package com.github.catvod.utils;

import static fi.iki.elonen.NanoHTTPD.Response.Status;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.os.SystemClock;
import android.text.TextUtils;

import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;
import com.github.catvod.spider.Proxy;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import okhttp3.Response;

public class ProxyVideo {

    private static final String GO_SERVER = "http://127.0.0.1:7777/";

    public static void go() {
        boolean close = OkHttp.string(GO_SERVER).isEmpty();
        if (close) OkHttp.string("http://127.0.0.1:" + Proxy.getPort() + "/go");
        if (close) while (OkHttp.string(GO_SERVER).isEmpty()) SystemClock.sleep(20);
    }

    public static String goVer() {
        try {
            go();
            String result = OkHttp.string(GO_SERVER + "version");
            return new JSONObject(result).optString("version");
        } catch (Exception e) {
            return "";
        }
    }

    public static String url(String url, int thread) {
        if (!TextUtils.isEmpty(goVer()) && url.contains("/proxy?")) url += "&response=url";
        return String.format(Locale.getDefault(), "%s?url=%s&thread=%d", GO_SERVER, URLEncoder.encode(url), thread);
    }

    public static Object[] proxy(String url, Map<String, String> headers) throws Exception {
        Object[] temp = new Object[4];
        Response response = OkHttp.newCall(url, headers);
        SpiderDebug.log("proxy url" + url  + " headers: " + headers.toString());
        String contentType = response.headers().get("Content-Type");
        String hContentLength = response.headers().get("Content-Length");
        String contentDisposition = response.headers().get("Content-Disposition");
        long contentLength = hContentLength != null ? Long.parseLong(hContentLength) : 0;
        if (contentDisposition != null) contentType = getMimeType(contentDisposition);
        Map oHeaders = new HashMap();
        List<String> arr = Arrays.asList("Accept-Ranges","Content-Range");
//        oHeaders.put("Accept-Ranges", "bytes");
//        oHeaders.put("Content-Range", response.headers().get("Content-Range"));
//        NanoHTTPD.Response resp = newFixedLengthResponse(Status.PARTIAL_CONTENT, contentType, response.body().byteStream(), contentLength);
        for (String key : response.headers().names()) oHeaders.put(key, response.headers().get(key));
        temp[0] = Status.PARTIAL_CONTENT.getRequestStatus();
        temp[1] = contentType;
        temp[2] = response.body().byteStream();
        temp[3] = oHeaders;
        return temp;
    }

    private static String getMimeType(String contentDisposition) {
        if (contentDisposition.endsWith(".mp4")) {
            return "video/mp4";
        } else if (contentDisposition.endsWith(".webm")) {
            return "video/webm";
        } else if (contentDisposition.endsWith(".avi")) {
            return "video/x-msvideo";
        } else if (contentDisposition.endsWith(".wmv")) {
            return "video/x-ms-wmv";
        } else if (contentDisposition.endsWith(".flv")) {
            return "video/x-flv";
        } else if (contentDisposition.endsWith(".mov")) {
            return "video/quicktime";
        } else if (contentDisposition.endsWith(".mkv")) {
            return "video/x-matroska";
        } else if (contentDisposition.endsWith(".mpeg")) {
            return "video/mpeg";
        } else if (contentDisposition.endsWith(".3gp")) {
            return "video/3gpp";
        } else if (contentDisposition.endsWith(".ts")) {
            return "video/MP2T";
        } else if (contentDisposition.endsWith(".mp3")) {
            return "audio/mp3";
        } else if (contentDisposition.endsWith(".wav")) {
            return "audio/wav";
        } else if (contentDisposition.endsWith(".aac")) {
            return "audio/aac";
        } else {
            return null;
        }
    }
}
