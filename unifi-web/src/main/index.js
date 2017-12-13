import React, { Component } from 'react'
import { Provider } from 'react-redux'
import createHistory from 'history/createBrowserHistory'
import { Router, Route, Switch /* Redirect */ } from 'react-router'

import store from './store'
import * as ROUTES from '../utils/routes'

import {
  Evacuation, NotFound, Sitemap, MyAccount, Login,
  Discovery, LiveView, SiteManager, Users, Navigation,
  ClientRegistry,
} from '../pages'

export default class Main extends Component {
  render() {
    return (
      <Provider store={store}>
        <Router history={createHistory()}>
          <Switch>
            <Route exact path={ROUTES.SITEMAP} component={Sitemap} />
            <Route exact path={ROUTES.LOGIN} component={Login} />
            <Route exact path={ROUTES.MY_ACCOUNT} component={MyAccount} />
            <Route exact path={ROUTES.DIRECTORY} component={Discovery} />

            <Route path={ROUTES.EVACUATION} component={Evacuation} />
            <Route path={ROUTES.CLIENT_REGISTRY} component={ClientRegistry} />

            <Route exact path={ROUTES.LIVE_VIEW} component={LiveView} />
            <Route exact path={ROUTES.NAVIGATION} component={Navigation} />
            <Route exact path={ROUTES.SITE_MANAGER} component={SiteManager} />
            <Route exact path={ROUTES.USERS} component={Users} />

            <Route component={NotFound} />
          </Switch>
        </Router>
      </Provider>
    )
  }
}
