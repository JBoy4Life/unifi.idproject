import React from 'react'
import { Route, Router, Switch, Redirect } from 'react-router'
import { userIsAuthenticatedRedir, userIsNotAuthenticatedRedir } from 'hocs/auth'

import * as ROUTES from 'utils/routes'

import {
  Evacuation, NotFound, Sitemap, MyAccount, Login,
  Directory, LiveView, SiteManager, Users, Navigation,
  ClientRegistry, Attendance, ForgotPassword, ResetPassword,
  CancelPasswordReset, ChangePassword
} from '../pages'

const ProtectedRoutes = userIsAuthenticatedRedir(() => (
  <Switch>
    <Route exact path={ROUTES.SITEMAP}component={Sitemap} />

    <Route exact path={ROUTES.MY_ACCOUNT}component={MyAccount} />
    <Route path={ROUTES.DIRECTORY} component={Directory} />

    <Route exact path={ROUTES.CHANGE_PASSWORD} component={ChangePassword} />

    <Route path={ROUTES.EVACUATION} component={Evacuation} />
    <Route path={ROUTES.ATTENDANCE} component={Attendance} />

    <Route path={ROUTES.CLIENT_REGISTRY} component={ClientRegistry} />

    <Route path={ROUTES.LIVE_VIEW} component={LiveView} />

    <Route exact path={ROUTES.NAVIGATION}component={Navigation} />

    <Route path={ROUTES.SITE_MANAGER} component={SiteManager} />

    <Route exact path={ROUTES.USERS}component={Users} />

    <Redirect exact from="/" to={ROUTES.LIVE_VIEW} />
  </Switch>
))

export default ({ history }) => (
  <Router history={history}>
    <Switch>
      <Route exact path={ROUTES.LOGIN} component={Login} />
      <Route exact path={ROUTES.ACCEPT_INVITATION} component={ResetPassword} />
      <Route exact path={ROUTES.FORGOT_PASSWORD} component={ForgotPassword} />
      <Route exact path={ROUTES.CANCEL_PASSWORD_RESET} component={CancelPasswordReset} />
      <Route exact path={ROUTES.RESET_PASSWORD} component={ResetPassword} />
      <Route exact path={ROUTES.NOT_FOUND} component={NotFound} />
      <Route path="/" component={ProtectedRoutes} />

      <Route component={NotFound} />
    </Switch>
  </Router>
)
