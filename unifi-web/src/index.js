import React from 'react'
import ReactDOM from 'react-dom'

import './index.css'

import Main from './main'
import registerServiceWorker from './registerServiceWorker'
import { WSProtocol, WSPackage } from './lib/ws'

const wsProtocol = new WSProtocol({ url: 'ws://localhost:8080' })

wsProtocol
  .connect()
  .then(() => {
    wsProtocol.start()

    const pack = new WSPackage({
      type: 'beep',
      payload: { message: 'Hello Server!' },
    })

    wsProtocol
      .request(pack)
      .then((response) => {
        console.log('received response for', pack.content.corelationId, response)
      })
      .catch((err) => {
        console.log('received error for', pack.content.corelationId, err)
      })
  })
  .catch((err) => {
    console.error(err)
  })

ReactDOM.render(<Main />, document.getElementById('root'))
registerServiceWorker()
