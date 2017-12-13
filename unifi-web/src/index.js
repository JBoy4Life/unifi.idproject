import React from 'react'
import ReactDOM from 'react-dom'

import './index.css'

import Main from './main'
import registerServiceWorker from './registerServiceWorker'
import { WSProtocol } from './lib/ws'

// const wsProtocol = new WSProtocol({ url: 'ws://127.0.0.1:8000/service/msgpack' })
const wsProtocol = new WSProtocol({ url: 'ws://127.0.0.1:8000/service/json' })
window.wsProtocol = wsProtocol
wsProtocol
  .connect()
  .then(() => {
    wsProtocol.start()

    // const pack = new WSPackage({
    //   protocolVersion: '1.0.0',
    //   releaseVersion: '1.0.0',
    //   messageType: 'core.client.register-client',
    //   payload: {
    //     clientId: 'deloitte3',
    //     displayName: 'Deloitte3',
    //     logo: '639rcvjcU2wkq9jdWt3igpaze90GcR6wZvXO/SDpCbMScaWIO8Vw2jk2fmiTeQEu248uSEX5upYVuB3ioLqIQA==',
    //   },
    // })

    // wsProtocol
    //   .request(pack.content, { json: true })
    //   .then((response) => {
    //     console.log('received response for', pack.content.correlationId, response)
    //   })
    //   .catch((err) => {
    //     console.log('received error for', pack.content.correlationId, err)
    //   })
  })
  .catch((err) => {
    console.error(err)
  })

ReactDOM.render(<Main />, document.getElementById('root'))
registerServiceWorker()
