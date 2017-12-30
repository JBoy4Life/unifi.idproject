import React, { Component } from 'react'
import { Provider } from 'react-redux'
import { PersistGate } from 'redux-persist/es/integration/react'
import createHistory from 'history/createBrowserHistory'
import { Router, Route, Switch, /* Redirect */
  Redirect } from 'react-router'

import { WSProtocol } from '../lib/ws'

import { configureStore } from './store'
import * as ROUTES from '../utils/routes'

import {
  Evacuation, NotFound, Sitemap, MyAccount, Login,
  Discovery, LiveView, SiteManager, Users, Navigation,
  ClientRegistry, Attendance
} from '../pages'

import { selectors as userSelectors } from '../reducers/user'

// // README. Avoid using actions like this. Normally, acitons
// // should be called by invoking the aciton cretors (see acitons.js files)
// // In case of login tests this will work better, but in general it is not
// // a good idea to go this way.
// import { USER_SET } from '../reducers/user/types'

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

        store.subscribe(() => {
          this.setState({
            ...userSelectors.getReducer(store.getState()),
          })
        })

        this.setState({
          store,
          persistor,
          loading: false,
          history: createHistory(),
        })
      })
      .catch((err) => {
        console.error(err)
      })
  }

  renderContent() {
    const { currentUser, initialising } = this.state

    if (initialising) {
      return 'Loading'
    }

    if (!currentUser) {
      return <Login />
    }

    return (
      <Router history={this.state.history}>
        <Switch>
          <Route exact path={ROUTES.SITEMAP} component={Sitemap} />
          <Route exact path={ROUTES.MY_ACCOUNT} component={MyAccount} />
          <Route exact path={ROUTES.DIRECTORY} component={Discovery} />

          <Route path={ROUTES.EVACUATION} component={Evacuation} />
          <Route path={ROUTES.ATTENDANCE} component={Attendance} />

          <Route path={ROUTES.CLIENT_REGISTRY} component={ClientRegistry} />

          <Route path={ROUTES.LIVE_VIEW} component={LiveView} />

          <Route exact path={ROUTES.NAVIGATION} component={Navigation} />

          <Route path={ROUTES.SITE_MANAGER} component={SiteManager} />

          <Route exact path={ROUTES.USERS} component={Users} />

          <Redirect exact from={ROUTES.LOGIN} to={ROUTES.SITEMAP} />
          <Route component={NotFound} />
        </Switch>

      </Router>
    )
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
          {this.renderContent()}
        </PersistGate>
      </Provider>
    )
  }
}
