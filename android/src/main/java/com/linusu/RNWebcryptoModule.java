package com.linusu;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.GCMParameterSpec;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import android.util.Base64;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

class CryptoKey {
  final String id;
  final SecretKeySpec spec;
  final Boolean extractable;
  final Set<String> usages;

  public CryptoKey(SecretKeySpec spec, Boolean extractable, String encodedUsages) {
    this.id = UUID.randomUUID().toString();
    this.spec = spec;
    this.extractable = extractable;
    this.usages = new HashSet<String>();

    StringTokenizer st = new StringTokenizer(encodedUsages, ",");
    while(st.hasMoreTokens()) this.usages.add(st.nextToken());
  }
}

public class RNWebcryptoModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;
  private final Map<String, CryptoKey> keys;

  public RNWebcryptoModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.keys = new HashMap<String, CryptoKey>();
  }

  @Override
  public String getName() {
    return "RNWebcrypto";
  }

  @ReactMethod
  public void importKey(String format, String encodedKeyData, String algo, Boolean extractable, String encodedUsages, final Promise promise) {
    assert algo == "AES-GCM";

    byte[] keyData = Base64.decode(encodedKeyData, Base64.DEFAULT);
    CryptoKey cryptoKey = new CryptoKey(new SecretKeySpec(keyData, "AES"), extractable, encodedUsages);

    this.keys.put(cryptoKey.id, cryptoKey);

    promise.resolve(cryptoKey.id);
  }

  @ReactMethod
  void encryptAesGcm(String keyId, String encodedIv, String encodedData, Promise promise) throws Exception {
    CryptoKey cryptoKey = this.keys.get(keyId);

    if (!cryptoKey.usages.contains("encrypt")) {
      promise.reject("INVALID_ACCESS", "This key cannot be used for encrypting");
      return;
    }

    byte[] iv = Base64.decode(encodedIv, Base64.DEFAULT);
    byte[] data = Base64.decode(encodedData, Base64.DEFAULT);

    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

    cipher.init(Cipher.ENCRYPT_MODE, cryptoKey.spec, new GCMParameterSpec(128, iv));

    byte[] encrypted = cipher.doFinal(data);

    promise.resolve(Base64.encodeToString(encrypted, Base64.DEFAULT));
  }

  @ReactMethod
  void decryptAesGcm(String keyId, String encodedIv, String encodedData, Promise promise) throws Exception {
    CryptoKey cryptoKey = this.keys.get(keyId);

    if (!cryptoKey.usages.contains("decrypt")) {
      promise.reject("INVALID_ACCESS", "This key cannot be used for decrypting");
      return;
    }

    byte[] iv = Base64.decode(encodedIv, Base64.DEFAULT);
    byte[] data = Base64.decode(encodedData, Base64.DEFAULT);

    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

    cipher.init(Cipher.DECRYPT_MODE, cryptoKey.spec, new GCMParameterSpec(128, iv));

    byte[] decrypted = cipher.doFinal(data);

    promise.resolve(Base64.encodeToString(decrypted, Base64.DEFAULT));
  }
}
