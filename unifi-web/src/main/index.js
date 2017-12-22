import React, { Component } from 'react'
import { Provider } from 'react-redux'
import { PersistGate } from 'redux-persist/es/integration/react'
import createHistory from 'history/createBrowserHistory'
import { Router, Route, Switch /* Redirect */ } from 'react-router'

import { WSProtocol } from '../lib/ws'

import { configureStore } from './store'
import * as ROUTES from '../utils/routes'

import {
  Evacuation, NotFound, Sitemap, MyAccount, Login,
  Discovery, LiveView, SiteManager, Users, Navigation,
  ClientRegistry,
} from '../pages'

export default class Main extends Component {
  state = {
    loading: true,
  }

  componentDidMount() {
    const wsProtocol = new WSProtocol({ url: `ws:/${process.env.SOCKET_URI}/service/json` })
    wsProtocol
      .connect()
      .then(() => wsProtocol.start())
      .then(() => {
        const { store, persistor } = configureStore(wsProtocol)
        this.setState({
          store,
          persistor,
          loading: false,
        })
      })
      .then(() => {
        // console.log('TEST_USER')
        this.state.store.dispatch({ type: 'TEST_USER', currentUser: 'vlad' })
      })
      .catch((err) => {
        console.error(err)
      })
  }

  render() {
    const { loading, store, persistor } = this.state
    return (

      loading ? <div>Loading</div> :
      <Provider store={store}>
        <PersistGate
          loading="loading"
          persistor={persistor}
        >
          <Router history={createHistory()}>

            <Switch>
              <Route exact path={ROUTES.SITEMAP} component={Sitemap} />
              <Route exact path={ROUTES.LOGIN} component={Login} />
              <Route exact path={ROUTES.MY_ACCOUNT} component={MyAccount} />
              <Route exact path={ROUTES.DIRECTORY} component={Discovery} />

              <Route path={ROUTES.EVACUATION} component={Evacuation} />
              <Route path={ROUTES.CLIENT_REGISTRY} component={ClientRegistry} />

              <Route path={ROUTES.LIVE_VIEW} component={LiveView} />

              <Route exact path={ROUTES.NAVIGATION} component={Navigation} />

              <Route path={ROUTES.SITE_MANAGER} component={SiteManager} />

              <Route exact path={ROUTES.USERS} component={Users} />

              <Route component={NotFound} />
            </Switch>

          </Router>
        </PersistGate>
      </Provider>
    )
  }
}
