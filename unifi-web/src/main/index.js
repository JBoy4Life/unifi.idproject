import React, { Component } from 'react'
import { Provider } from 'react-redux'
import { PersistGate } from 'redux-persist/es/integration/react'
import createHistory from 'history/createBrowserHistory'

import * as userActions from 'reducers/user/actions'
import Routes from './routes'
import { configureStore } from './store'
import { selectors as userSelectors } from 'reducers/user'
import { WSProtocol } from 'lib/ws'

export default class Main extends Component {
  state = {
    loading: true,
  }

  componentDidMount() {
    const wsProtocol = new WSProtocol({ url: `${process.env.SOCKET_PROTO}://${process.env.SOCKET_URI}/service/json` })
    wsProtocol
      .connect()
      .then(() => wsProtocol.start())
      .then(() => {
        const { store, persistor } = configureStore(wsProtocol)

        this.setState({
          store,
          persistor,
          history: createHistory(),
        })
      })
      .then(() => {
        // Reauthenticate if we have a session.
        const currentUser = localStorage.getItem('unifi-current-user') ?
          JSON.parse(localStorage.getItem('unifi-current-user')) :
          {}

        if (currentUser && currentUser.token) {
          const action = userActions.reauthenticateRequest(currentUser.token)
          return wsProtocol.request(action.socketRequest, { json: true })
        }
      })
      .then(() => {
        this.setState({
          loading: false
        })
      })
      .catch((err) => {
        console.error(err)
      })
  }

  render() {
    const { history, loading, store, persistor } = this.state

    return loading ? <div>Loading...</div> : (
      <Provider store={store}>
        <PersistGate
          loading="loading"
          persistor={persistor}
        >
          <Routes history={history} />
        </PersistGate>
      </Provider>
    )
  }
}
