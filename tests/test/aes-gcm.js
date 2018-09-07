/* eslint-env mocha */

import assert from 'assert'
import base64Decode from 'fast-base64-decode'

const keyData1 = new Uint8Array(32)
const iv1 = new Uint8Array(12)
const plaintext1 = new Uint8Array(27)
const ciphertext1 = new Uint8Array(43)

base64Decode('7v3WhTjbRFh74x35oHN4yymvGl5CCbpeovU00oBFzfg=', keyData1)
base64Decode('L5ZUkJk8+DJ6mLQ/', iv1)
base64Decode('RBpfDv8ZwzFvDiygxCuwQW0SDM3EdrtUAf8V', plaintext1)
base64Decode('FsZQWVYduNY4GZgcgvpFH4Nj8BEwtOJ4lp+L8tDYBFuiSnsqtC9jt47SJQ==', ciphertext1)

function stringifyArrayBuffer (ab) {
  return Array.from(new Uint8Array(ab)).join(',')
}

let key1

it('AES-GCM - imports a key', async () => {
  key1 = await crypto.subtle.importKey('raw', keyData1.buffer, 'AES-GCM', false, ['encrypt', 'decrypt'])
})

it('AES-GCM - encrypts a simple message', async () => {
  const result = await crypto.subtle.encrypt({ name: 'AES-GCM', iv: iv1.buffer }, key1, plaintext1.buffer)
  assert.strictEqual(stringifyArrayBuffer(result), stringifyArrayBuffer(ciphertext1))
})

it('AES-GCM - decrypts a simple message', async () => {
  const result = await crypto.subtle.decrypt({ name: 'AES-GCM', iv: iv1.buffer }, key1, ciphertext1.buffer)
  assert.strictEqual(stringifyArrayBuffer(result), stringifyArrayBuffer(plaintext1))
})
