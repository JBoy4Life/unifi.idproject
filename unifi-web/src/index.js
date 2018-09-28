import React from 'react'
import ReactDOM from 'react-dom'
import { PersistGate } from 'redux-persist/es/integration/react'
import { Provider } from 'react-redux'

import Loading from 'components/loading'
import Main from './main'
import registerServiceWorker from './registerServiceWorker'
import { configureStore, history } from 'redux/store'
import UnifiWsClient from 'lib/unifi-ws-client'

import './styles/less/index.less'
import './styles/index.scss'

export const clientId = window.location.hostname.split(".")[0];
export const socketUri = process.env.SOCKET_URI;

ReactDOM.render(<Loading />, document.getElementById('root'))

const wsClient = new UnifiWsClient({
  url: `${process.env.SOCKET_PROTO}://${process.env.SOCKET_URI}/service/${process.env.REACT_APP_WS_MESSAGE_FORMAT}`,
  type: process.env.REACT_APP_WS_MESSAGE_FORMAT
})

wsClient
  .connect()
  .then(() => {
    wsClient.start()
    const { store, persistor } = configureStore(wsClient)
    ReactDOM.render(
      <Provider store={store}>
        <PersistGate
          loading={<Loading />}
          persistor={persistor}
        >
          <Main history={history} />
        </PersistGate>
      </Provider>,
      document.getElementById('root')
    )
  })
  .catch((err) => {
    console.error(err)
  })

registerServiceWorker();
