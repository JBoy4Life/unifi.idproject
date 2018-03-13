import React from 'react'
import { Route, Router, Switch, Redirect } from 'react-router'

import * as ROUTES from 'utils/routes'

import {
  Evacuation, NotFound, Sitemap, MyAccount, Login,
  Directory, LiveView, SiteManager, Operators, Navigation,
  ClientRegistry, Attendance, ForgotPassword, ResetPassword,
  CancelPasswordReset, ChangePassword
} from '../pages'


export default ({ history }) => (
  <Router history={history}>
    <Switch>
      <Route exact path={ROUTES.LOGIN} component={Login} />
      <Route exact path={ROUTES.ACCEPT_INVITATION} component={ResetPassword} />
      <Route exact path={ROUTES.FORGOT_PASSWORD} component={ForgotPassword} />
      <Route exact path={ROUTES.CANCEL_PASSWORD_RESET} component={CancelPasswordReset} />
      <Route exact path={ROUTES.RESET_PASSWORD} component={ResetPassword} />
      <Route exact path={ROUTES.NOT_FOUND} component={NotFound} />

      <Route exact path={ROUTES.CHANGE_PASSWORD} component={ChangePassword} />

      <Route exact path={ROUTES.SITEMAP}component={Sitemap} />
      <Route exact path={ROUTES.MY_ACCOUNT}component={MyAccount} />
      <Route exact path={ROUTES.NAVIGATION}component={Navigation} />

      <Route path={ROUTES.LIVE_VIEW} component={LiveView} />
      <Route path={ROUTES.ATTENDANCE} component={Attendance} />
      <Route path={ROUTES.DIRECTORY} component={Directory} />
      <Route path={ROUTES.OPERATORS} component={Operators} />

      <Route path={ROUTES.SITE_MANAGER} component={SiteManager} />
      <Route path={ROUTES.EVACUATION} component={Evacuation} />
      <Route path={ROUTES.CLIENT_REGISTRY} component={ClientRegistry} />

      <Redirect exact from="/" to={ROUTES.LIVE_VIEW} />
      <Redirect to={ROUTES.NOT_FOUND} component={NotFound} />
    </Switch>
  </Router>
)
