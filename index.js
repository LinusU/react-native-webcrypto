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

function stringify (data) {
  return JSON.stringify(JSON.stringify(data, (key, value) => {
    if (value instanceof ArrayBuffer) return `FC27F74E-7BDE-49A8-8696-F5453810E05D:${base64Encode(new Uint8Array(value))}`
    if (value instanceof Uint8Array) return `2DA57A17-C158-4177-BC4E-2EAE1A21D25B:${base64Encode(value)}`
    if (value instanceof CryptoKey) return `1F0A2C3A-F721-417C-9C67-9204BAB92CA3:${internalKeyMap.get(value)}`

    return value
  }))
}

const subtle = {
  async importKey (format, keyData, algo, extractable, usages) {
    if (format !== 'raw') throw new Error(`Format not implemented: ${format}`)
    if (!(keyData instanceof ArrayBuffer)) throw new TypeError('keyData must be an ArrayBuffer')
    if (typeof extractable !== 'boolean') throw new TypeError('extractable must be a boolean')
    if (!Array.isArray(usages)) throw new TypeError('usages must be an array')

    const keyId = await RNWebcrypto.importKey(stringify([format, keyData, algo, extractable, usages]))
    const key = new CryptoKey('secret', extractable, algo, usages)

    internalKeyMap.set(key, keyId)

    return key
  },

  async encrypt (algorithm, key, data) {
    if (!(key instanceof CryptoKey)) throw new TypeError('key must be an CryptoKey')
    if (!(data instanceof ArrayBuffer)) throw new TypeError('data must be an ArrayBuffer')

    if (!internalKeyMap.has(key)) throw new Error('Invalid key')

    return decodeToBuffer(await RNWebcrypto.encrypt(stringify([algorithm, key, data])))
  },

  async decrypt (algorithm, key, data) {
    if (!(key instanceof CryptoKey)) throw new TypeError('key must be an CryptoKey')
    if (!(data instanceof ArrayBuffer)) throw new TypeError('data must be an ArrayBuffer')

    if (!internalKeyMap.has(key)) throw new Error('Invalid key')

    return decodeToBuffer(await RNWebcrypto.decrypt(stringify([algorithm, key, data])))
  },

  async deriveBits (algorithm, key, bits) {
    if (!(key instanceof CryptoKey)) throw new TypeError('key must be an CryptoKey')
    if (typeof bits !== 'number') throw new TypeError('bits must be a number')
    if (bits < 0 || !Number.isInteger(bits)) throw new TypeError('bits must be a positive integer')

    if (!internalKeyMap.has(key)) throw new Error('Invalid key')

    return decodeToBuffer(await RNWebcrypto.deriveBits(stringify([algorithm, key, bits])))
  },
}

if (typeof global.crypto !== 'object') {
  global.crypto = {}
}

if (typeof global.crypto.subtle !== 'object') {
  global.crypto.subtle = subtle
}
