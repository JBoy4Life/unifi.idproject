import React, { Component } from 'react'
import { Provider } from 'react-redux'
import createHistory from 'history/createBrowserHistory'
import { Router, Route, Switch /* Redirect */ } from 'react-router'

import store from './store'
import * as ROUTES from '../utils/routes'

import { NotFound, Sitemap, MyAccount, Login } from '../pages'

export default class Main extends Component {
  render() {
    return (
      <Provider store={store}>
        <Router history={createHistory()}>
          <Switch>
            <Route exact path={ROUTES.SITEMAP} component={Sitemap} />
            <Route exact path={ROUTES.MY_ACCOUNT} component={MyAccount} />
            <Route exact path={ROUTES.LOGIN} component={Login} />
            <Route component={NotFound} />
          </Switch>
        </Router>
      </Provider>
    )
  }
}
