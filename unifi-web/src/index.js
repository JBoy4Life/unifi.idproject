import React from 'react'
import ReactDOM from 'react-dom'
import { PersistGate } from 'redux-persist/es/integration/react'
import { Provider } from 'react-redux'

import Loading from 'components/loading'
import Main from './main'
import registerServiceWorker from './registerServiceWorker'
import { configureStore, history } from 'redux/store'
import { WSProtocol } from 'lib/ws'

import './styles/less/index.less'
import './styles/index.scss'

export const clientId = window.location.hostname.split(".")[0];

ReactDOM.render(<Loading />, document.getElementById('root'))

const wsProtocol = new WSProtocol({ url: `${process.env.SOCKET_PROTO}://${process.env.SOCKET_URI}/service/json` })
wsProtocol
  .connect()
  .then(() => wsProtocol.start())
  .then(() => {
    const { store, persistor } = configureStore(wsProtocol)
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
