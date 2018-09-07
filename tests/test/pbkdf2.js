/* eslint-env mocha */

import assert from 'assert'
import base64Decode from 'fast-base64-decode'

const password = new Uint8Array(11)
const salt = new Uint8Array(16)
const plaintext = new Uint8Array(32)

base64Decode('fD5dc569kNNqR0o=', password)
base64Decode('ho3bGL+JAkXM/XmGavHARg==', salt)
base64Decode('DmPdp4y1ktLEiUf5bSAbb8bbKPiUch+r9N0MkQSJtrc=', plaintext)

function stringifyArrayBuffer (ab) {
  return Array.from(new Uint8Array(ab)).join(',')
}

it('PBKDF2 - derives bits', async () => {
  const key = await crypto.subtle.importKey('raw', password.buffer, { name: 'PBKDF2' }, false, ['deriveBits'])
  const result = await crypto.subtle.deriveBits({ name: 'PBKDF2', salt: salt.buffer, iterations: 500, hash: { name: 'SHA-512' } }, key, 256)

  assert.strictEqual(stringifyArrayBuffer(result), stringifyArrayBuffer(plaintext))
})
