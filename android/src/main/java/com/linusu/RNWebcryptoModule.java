package com.linusu;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

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
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;

import android.util.Base64;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

// import org.spongycastle.jce.provider.BouncyCastleProvider;

import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.digests.SHA512Digest;
import org.spongycastle.crypto.digests.GeneralDigest;
import org.spongycastle.crypto.params.KeyParameter;

// import com.facebook.android.crypto.keychain.AndroidConceal;
// import com.facebook.cipher.jni.PBKDF2Hybrid;

class CryptoKey {
  final String id;
  final String algorithm;
  final byte[] data;
  final Boolean extractable;
  final Set<String> usages;

  public CryptoKey(String algorithm, byte[] data, Boolean extractable, String encodedUsages) {
    this.id = UUID.randomUUID().toString();
    this.algorithm = algorithm;
    this.data = data;
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

    // Security.insertProviderAt(new BouncyCastleProvider(), 1);
    // try { AndroidConceal.get().nativeLibrary.ensureCryptoLoaded(); } catch (Exception e) {}
  }

  @Override
  public String getName() {
    return "RNWebcrypto";
  }

  @ReactMethod
  public void importKey(String format, String encodedKeyData, String algorithm, Boolean extractable, String encodedUsages, final Promise promise) {
    assert format == "raw";

    byte[] keyData = Base64.decode(encodedKeyData, Base64.DEFAULT);
    CryptoKey cryptoKey = new CryptoKey(algorithm, keyData, extractable, encodedUsages);

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

    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(cryptoKey.data, "AES"), new GCMParameterSpec(128, iv));

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

    cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(cryptoKey.data, "AES"), new GCMParameterSpec(128, iv));

    byte[] decrypted = cipher.doFinal(data);

    promise.resolve(Base64.encodeToString(decrypted, Base64.DEFAULT));
  }

  @ReactMethod
  void deriveBitsPbkdf2(String keyId, String encodedSalt, int iterationCount, int keyLength, Promise promise) throws Exception {
    CryptoKey cryptoKey = this.keys.get(keyId);

    if (!cryptoKey.usages.contains("deriveBits")) {
      promise.reject("INVALID_ACCESS", "This key cannot be used for deriving bits");
      return;
    }

    byte[] salt = Base64.decode(encodedSalt, Base64.DEFAULT);
    PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());

    gen.init(cryptoKey.data, salt, iterationCount);

    byte[] derived = ((KeyParameter) gen.generateDerivedParameters(keyLength)).getKey();


    // PBKDF2Hybrid encryptionKeyGenerator = new PBKDF2Hybrid();
    // encryptionKeyGenerator.setIterations(iterationCount);
    // encryptionKeyGenerator.setSalt(salt, 0, salt.length);
    // encryptionKeyGenerator.setKeyLengthInBytes(keyLength);
    // encryptionKeyGenerator.setPassword(cryptoKey.data, 0, cryptoKey.data.length);

    // byte[] derived = encryptionKeyGenerator.generate();



    promise.resolve(Base64.encodeToString(derived, Base64.DEFAULT));
  }
}
