/* eslint-env mocha */

import assert from 'assert'
import base64Decode from 'fast-base64-decode'

const password = new Uint8Array(11)
const salt = new Uint8Array(16)

base64Decode('fD5dc569kNNqR0o=', password)
base64Decode('ho3bGL+JAkXM/XmGavHARg==', salt)

function stringifyArrayBuffer (ab) {
  return Array.from(new Uint8Array(ab)).join(',')
}

it('pbkdf2', async () => {
  const key = await crypto.subtle.importKey('raw', password.buffer, { name: 'PBKDF2' }, false, ['deriveBits'])
  const result = await crypto.subtle.deriveBits({ name: 'PBKDF2', salt: salt.buffer, iterations: 500000, hash: { name: 'SHA-512' } }, key, 256)

  // assert.strictEqual(stringifyArrayBuffer(result), stringifyArrayBuffer(plaintext1))
})
