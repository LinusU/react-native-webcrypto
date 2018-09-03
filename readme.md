# react-native-webcrypto

WebCrypto implementation for React Native.

## Installation

```sh
npm install --save react-native-webcrypto
react-native link
```

## Usage

```javascript
import 'react-native-webcrypto'

// The WebCrypto API is now available:
crypto.subtle
```

## Implementation Status

Feature | iOS | Android
----- | ------ | ------
**AES-GCM** | |
&mdash; `importKey` | 🚫 | ✅
&mdash; `encrypt` | 🚫 | ✅
&mdash; `decrypt` | 🚫 | ✅
