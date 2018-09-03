import { NativeModules } from 'react-native'
import base64Decode from 'fast-base64-decode'
import base64Encode from 'fast-base64-encode'
import base64Length from 'fast-base64-length'

const { RNWebcrypto } = NativeModules

function decodeToBuffer (encoded) {
  encoded = encoded.trim()

  const length = base64Length(encoded)
  const result = new Uint8Array(length)

  base64Decode(encoded, result)

  return result.buffer
}

class CryptoKey {
  constructor (type, extractable, algorithm, usages) {
    Object.defineProperty(this, 'type', { value: type, enumerable: true })
    Object.defineProperty(this, 'extractable', { value: extractable, enumerable: true })
    Object.defineProperty(this, 'algorithm', { value: algorithm, enumerable: true })
    Object.defineProperty(this, 'usages', { value: usages, enumerable: true })
  }
}

/** @type WeakMap<CryptoKey,string> */
const internalKeyMap = new WeakMap()

const subtle = {
  /**
   * @param {'raw'} format
   * @param {ArrayBuffer} keyData
   * @param {'AES-GCM'} algo
   * @param {boolean} extractable
   * @param {('encrypt' | 'decrypt')[]} usages
   * @returns {Promise<CryptoKey>}
   */
  async importKey (format, keyData, algo, extractable, usages) {
    if (format !== 'raw') throw new Error(`Format not implemented: ${format}`)
    if (!(keyData instanceof ArrayBuffer)) throw new TypeError('keyData must be an ArrayBuffer')
    if (algo !== 'AES-GCM') throw new Error(`Algorithm not implemented: ${algo}`)
    if (typeof extractable !== 'boolean') throw new TypeError('extractable must be a boolean')
    if (!Array.isArray(usages)) throw new TypeError('usages must be an array')

    for (const usage of usages) {
      if (usage !== 'encrypt' && usage !== 'decrypt') throw new Error(`Usage not implemented: ${usage}`)
    }

    const keyId = await RNWebcrypto.importKey(format, base64Encode(new Uint8Array(keyData)), algo, extractable, usages.join(','))
    const key = new CryptoKey('secret', extractable, algo, usages)

    internalKeyMap.set(key, keyId)

    return key
  },

  /**
   * @param {{ name: 'AES-GCM', iv: ArrayBuffer }} algorithm
   * @param {CryptoKey} key
   * @param {ArrayBuffer} data
   * @returns {Promise<ArrayBuffer>}
   */
  async encrypt (algorithm, key, data) {
    if (typeof algorithm !== 'object') throw new Error(`Algorithm not implemented: ${algorithm}`)
    if (algorithm.name !== 'AES-GCM') throw new Error(`Algorithm not implemented: ${algorithm.name}`)
    if (!(algorithm.iv instanceof ArrayBuffer)) throw new TypeError('algorithm.iv must be an ArrayBuffer')
    if ('additionalData' in algorithm) throw new Error('additionalData not supported')
    if ('tagLength' in algorithm) throw new Error('tagLength not supported')

    if (!(key instanceof CryptoKey)) throw new TypeError('key must be an CryptoKey')
    if (!(data instanceof ArrayBuffer)) throw new TypeError('data must be an ArrayBuffer')

    if (!internalKeyMap.has(key)) throw new Error('Invalid key')

    const encoded = await RNWebcrypto.encryptAesGcm(internalKeyMap.get(key), base64Encode(new Uint8Array(algorithm.iv)), base64Encode(new Uint8Array(data)))

    return decodeToBuffer(encoded)
  },

  /**
   * @param {{ name: 'AES-GCM', iv: ArrayBuffer }} algorithm
   * @param {CryptoKey} key
   * @param {ArrayBuffer} data
   * @returns {Promise<ArrayBuffer>}
   */
  async decrypt (algorithm, key, data) {
    if (typeof algorithm !== 'object') throw new Error(`Algorithm not implemented: ${algorithm}`)
    if (algorithm.name !== 'AES-GCM') throw new Error(`Algorithm not implemented: ${algorithm.name}`)
    if (!(algorithm.iv instanceof ArrayBuffer)) throw new TypeError('algorithm.iv must be an ArrayBuffer')
    if ('additionalData' in algorithm) throw new Error('additionalData not supported')
    if ('tagLength' in algorithm) throw new Error('tagLength not supported')

    if (!(key instanceof CryptoKey)) throw new TypeError('key must be an CryptoKey')
    if (!(data instanceof ArrayBuffer)) throw new TypeError('data must be an ArrayBuffer')

    if (!internalKeyMap.has(key)) throw new Error('Invalid key')

    const encoded = await RNWebcrypto.decryptAesGcm(internalKeyMap.get(key), base64Encode(new Uint8Array(algorithm.iv)), base64Encode(new Uint8Array(data)))

    return decodeToBuffer(encoded)
  },
}

if (typeof global.crypto !== 'object') {
  global.crypto = {}
}

if (typeof global.crypto.subtle !== 'object') {
  global.crypto.subtle = subtle
}
