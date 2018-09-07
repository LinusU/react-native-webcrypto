package com.linusu;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.util.Base64;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;

public class RNWebcryptoModule extends ReactContextBaseJavaModule {
  private static final String HTML_SOURCE = (
    "<!DOCTYPE html>\n" +
    "<html>\n" +
    "<head></head>\n" +
    "<body>\n" +
    "  <script>\n" +
    // Imported from fast-base64-length
    "    function base64Length (source) {\n" +
    "      var sourceLength = source.length\n" +
    "      var paddingLength = (source[sourceLength - 2] === '=' ? 2 : (source[sourceLength - 1] === '=' ? 1 : 0))\n" +
    "      var baseLength = ((sourceLength - paddingLength) & 0xfffffffc) >> 2\n" +
    "    \n" +
    "      return (baseLength * 3) + (paddingLength >>> 1) + (paddingLength << 1 & 2)\n" +
    "    }\n" +
    "    \n" +
    // Imported from fast-base64-decode
    "    var DECODE_LOOKUP = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 62, 0, 62, 0, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 0, 0, 0, 0, 63, 0, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51]\n" +
    "    function base64Decode (source, target) {\n" +
    "      var sourceLength = source.length\n" +
    "      var paddingLength = (source[sourceLength - 2] === '=' ? 2 : (source[sourceLength - 1] === '=' ? 1 : 0))\n" +
    "    \n" +
    "      var tmp\n" +
    "      var byteIndex = 0\n" +
    "      var baseLength = (sourceLength - paddingLength) & 0xfffffffc\n" +
    "    \n" +
    "      for (var i = 0; i < baseLength; i += 4) {\n" +
    "        tmp = (DECODE_LOOKUP[source.charCodeAt(i)] << 18) | (DECODE_LOOKUP[source.charCodeAt(i + 1)] << 12) | (DECODE_LOOKUP[source.charCodeAt(i + 2)] << 6) | (DECODE_LOOKUP[source.charCodeAt(i + 3)])\n" +
    "    \n" +
    "        target[byteIndex++] = (tmp >> 16) & 0xFF\n" +
    "        target[byteIndex++] = (tmp >> 8) & 0xFF\n" +
    "        target[byteIndex++] = (tmp) & 0xFF\n" +
    "      }\n" +
    "    \n" +
    "      if (paddingLength === 1) {\n" +
    "        tmp = (DECODE_LOOKUP[source.charCodeAt(i)] << 10) | (DECODE_LOOKUP[source.charCodeAt(i + 1)] << 4) | (DECODE_LOOKUP[source.charCodeAt(i + 2)] >> 2)\n" +
    "    \n" +
    "        target[byteIndex++] = (tmp >> 8) & 0xFF\n" +
    "        target[byteIndex++] = tmp & 0xFF\n" +
    "      }\n" +
    "    \n" +
    "      if (paddingLength === 2) {\n" +
    "        tmp = (DECODE_LOOKUP[source.charCodeAt(i)] << 2) | (DECODE_LOOKUP[source.charCodeAt(i + 1)] >> 4)\n" +
    "    \n" +
    "        target[byteIndex++] = tmp & 0xFF\n" +
    "      }\n" +
    "    }\n" +
    "    \n" +
    // Imported from fast-base64-encode
    "    var ENCODE_LOOKUP = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/'.split('')\n" +
    "    function base64Encode (source) {\n" +
    "      var target = ''\n" +
    "    \n" +
    "      var i, tmp\n" +
    "      var length = source.length\n" +
    "      var extraLength = length % 3\n" +
    "      var baseLength = length - extraLength\n" +
    "    \n" +
    "      for (i = 0; i < baseLength; i += 3) {\n" +
    "        tmp = (source[i] & 0xFF) << 16 | (source[i + 1] & 0xFF) << 8 | (source[i + 2] & 0xFF)\n" +
    "        target += (ENCODE_LOOKUP[tmp >> 18 & 0x3F] + ENCODE_LOOKUP[tmp >> 12 & 0x3F] + ENCODE_LOOKUP[tmp >> 6 & 0x3F] + ENCODE_LOOKUP[tmp & 0x3F])\n" +
    "      }\n" +
    "    \n" +
    "      if (extraLength === 1) {\n" +
    "        tmp = (source[baseLength] & 0xFF)\n" +
    "        target += ENCODE_LOOKUP[tmp >> 2] + ENCODE_LOOKUP[tmp << 4 & 0x3F] + '=='\n" +
    "      }\n" +
    "    \n" +
    "      if (extraLength === 2) {\n" +
    "        tmp = (source[baseLength] & 0xFF) << 8 | (source[baseLength + 1] & 0xFF)\n" +
    "        target += ENCODE_LOOKUP[tmp >> 10] + ENCODE_LOOKUP[tmp >> 4 & 0x3F] + ENCODE_LOOKUP[tmp << 2 & 0x3F] + '='\n" +
    "      }\n" +
    "    \n" +
    "      return target\n" +
    "    }\n" +
    "    \n" +
    // Helper function to decode into an Uint8Array
    "    function base64DecodeToUint8Array (input) {\n" +
    "      const result = new Uint8Array(base64Length(input))\n" +
    "      base64Decode(input, result)\n" +
    "      return result\n" +
    "    }\n" +
    "    \n" +
    // Reviver for supporting Uint8Array & ArrayBuffer in JSON.parse
    "    function revive (key, value) {\n" +
    "      if (typeof value === 'string' && value.startsWith('FC27F74E-7BDE-49A8-8696-F5453810E05D:')) return base64DecodeToUint8Array(value.slice(37)).buffer\n" +
    "      if (typeof value === 'string' && value.startsWith('2DA57A17-C158-4177-BC4E-2EAE1A21D25B:')) return base64DecodeToUint8Array(value.slice(37))\n" +
    "      if (typeof value === 'string' && value.startsWith('1F0A2C3A-F721-417C-9C67-9204BAB92CA3:')) return window.getKey(value.slice(37))\n" +
    "      return value\n" +
    "    }\n" +
    "    \n" +
    // "Public" API used by evaluateJavascript
    "    const keyStore = new Map()\n" +
    "    window.getKey = (id) => keyStore.get(id)\n" +
    "    window.putKey = (id, key) => { keyStore.set(id, key); return id }\n" +
    "    window.parse = (input) => JSON.parse(input, revive)\n" +
    "  </script>\n" +
    "</body>\n" +
    "</html>\n"
  );

  private final ReactApplicationContext reactContext;
  private final Map<String, Promise> requests;
  private WebView webView;

  public RNWebcryptoModule(final ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.requests = new HashMap<String, Promise>();

    final RNWebcryptoModule self = this;
    UiThreadUtil.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        self.webView = new WebView(reactContext);
        self.webView.getSettings().setJavaScriptEnabled(true);
        self.webView.addJavascriptInterface(self, "RNWebCrypto");
        self.webView.loadDataWithBaseURL("https://localhost/", HTML_SOURCE, "text/html", "UTF-8", "");
      }
    });
  }

  @Override
  public String getName() {
    return "RNWebcrypto";
  }

  private void evaluateJavascript (final String js) {
    final WebView webView = this.webView;
    UiThreadUtil.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        webView.evaluateJavascript(js, null);
      }
    });
  }

  @ReactMethod
  public void importKey(final String arguments, final Promise promise) {
    final String id = UUID.randomUUID().toString();

    this.requests.put(id, promise);
    this.evaluateJavascript("crypto.subtle.importKey(...parse(" + arguments + ")).then(key => RNWebCrypto.resolve('" + id + "', putKey('" + id + "', key)), err => RNWebCrypto.reject('" + id + "', String(err)))");
  }

  @ReactMethod
  public void encrypt(final String arguments, final Promise promise) {
    final String id = UUID.randomUUID().toString();

    this.requests.put(id, promise);
    this.evaluateJavascript("crypto.subtle.encrypt(...parse(" + arguments + ")).then(output => RNWebCrypto.resolve('" + id + "', base64Encode(new Uint8Array(output))), err => RNWebCrypto.reject('" + id + "', String(err)))");
  }

  @ReactMethod
  public void decrypt(final String arguments, final Promise promise) {
    final String id = UUID.randomUUID().toString();

    this.requests.put(id, promise);
    this.evaluateJavascript("crypto.subtle.decrypt(...parse(" + arguments + ")).then(output => RNWebCrypto.resolve('" + id + "', base64Encode(new Uint8Array(output))), err => RNWebCrypto.reject('" + id + "', String(err)))");
  }

  @ReactMethod
  public void deriveBits(final String arguments, final Promise promise) {
    final String id = UUID.randomUUID().toString();

    this.requests.put(id, promise);
    this.evaluateJavascript("crypto.subtle.deriveBits(...parse(" + arguments + ")).then(output => RNWebCrypto.resolve('" + id + "', base64Encode(new Uint8Array(output))), err => RNWebCrypto.reject('" + id + "', String(err)))");
  }

  @JavascriptInterface
  public void resolve(String requestId, String result) {
    this.requests.get(requestId).resolve(result);
  }

  @JavascriptInterface
  public void reject(String requestId, String error) {
    this.requests.get(requestId).reject(error);
  }
}
