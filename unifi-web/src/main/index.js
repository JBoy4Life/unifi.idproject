import React, { Component } from 'react'
import { Provider } from 'react-redux'
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
    const wsProtocol = new WSProtocol({ url: 'ws://127.0.0.1:8000/service/json' })
    wsProtocol
      .connect()
      .then(() => wsProtocol.start())
      .then(() => {
        this.setState({
          loading: false,
          store: configureStore(wsProtocol),
        })
      })
      .catch((err) => {
        console.error(err)
      })
  }

  render() {
    const { loading, store } = this.state

    return (

      loading ? <div>Loading</div> :
      <Provider store={store}>
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
      </Provider>
    )
  }
}
