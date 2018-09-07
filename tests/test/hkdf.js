/* eslint-env mocha */

import assert from 'assert'
import base64Decode from 'fast-base64-decode'

const input = new Uint8Array(11)
const salt = new Uint8Array(16)
const info = new Uint8Array(16)
const plaintext = new Uint8Array(32)

base64Decode('fD5dc569kNNqR0o=', input)
base64Decode('ho3bGL+JAkXM/XmGavHARg==', salt)
base64Decode('Rxx3uict5I+eP2t6pCssUQ==', info)
base64Decode('ylQctKyzIXzMGNTXOEL6Ds5y8sB9NHBbDQhg1v5p5ZU=', plaintext)

function stringifyArrayBuffer (ab) {
  return Array.from(new Uint8Array(ab)).join(',')
}

it('HKDF - derives bits', async () => {
  const key = await crypto.subtle.importKey('raw', input.buffer, { name: 'HKDF' }, false, ['deriveBits'])
  const result = await crypto.subtle.deriveBits({ name: 'HKDF', salt: salt.buffer, info: info.buffer, hash: 'SHA-512' }, key, 256)

  assert.strictEqual(stringifyArrayBuffer(result), stringifyArrayBuffer(plaintext))
})
