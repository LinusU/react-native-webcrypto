import 'react-native-webcrypto'
import { tests } from './hooks'

import React, { Component } from 'react'
import { AppRegistry, Text, View } from 'react-native'

import { name as appName } from './app.json'

import './test/aes-gcm'
import './test/hkdf'
import './test/pbkdf2'

class App extends Component {
  constructor (props) {
    super(props)
    this.state = {}
  }

  async componentDidMount () {
    for (const name of Object.keys(tests)) {
      this.setState({ [name]: 'running' })

      try {
        await tests[name]()
        this.setState({ [name]: 'success' })
      } catch (err) {
        console.error(err)
        this.setState({ [name]: 'error' })
      }
    }
  }

  render () {
    return React.createElement(View, {}, Object.keys(tests).map((name) => {
      const status = this.state[name] || 'waiting'

      return React.createElement(Text, { key: name }, `${status} - ${name}`)
    }))
  }
}

AppRegistry.registerComponent(appName, () => App)
