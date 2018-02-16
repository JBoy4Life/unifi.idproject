import React from 'react'
import { Route, Router, Switch, Redirect } from 'react-router'
import { CrumbRoute } from 'components'
import { userIsAuthenticatedRedir, userIsNotAuthenticatedRedir } from 'hocs/auth'

import * as ROUTES from 'utils/routes'

import {
  Evacuation, NotFound, Sitemap, MyAccount, Login,
  Discovery, LiveView, SiteManager, Users, Navigation,
  ClientRegistry, Attendance, ResetPassword
} from '../pages'

const ProtectedRoutes = userIsAuthenticatedRedir(() => (
  <Switch>
    <CrumbRoute exact path={ROUTES.SITEMAP} title="Sitemap" component={Sitemap} />

    <CrumbRoute exact path={ROUTES.MY_ACCOUNT} title="My Account" component={MyAccount} />
    <CrumbRoute exact path={ROUTES.DIRECTORY} title="Discovery" component={Discovery} />

    <CrumbRoute path={ROUTES.EVACUATION} title="Evacuation" component={Evacuation} />
    <CrumbRoute path={ROUTES.ATTENDANCE} title="Attendance" component={Attendance} />

    <CrumbRoute path={ROUTES.CLIENT_REGISTRY} title="client Registry" component={ClientRegistry} />

    <CrumbRoute path={ROUTES.LIVE_VIEW} title="Live View" component={LiveView} />

    <CrumbRoute exact path={ROUTES.NAVIGATION} title="Navigation" component={Navigation} />

    <CrumbRoute path={ROUTES.SITE_MANAGER} title="Site Manager" component={SiteManager} />

    <CrumbRoute exact path={ROUTES.USERS} title="Users" component={Users} />

    <Redirect from="/" to={ROUTES.ATTENDANCE} />
  </Switch>
))

export default ({ history }) => (
  <Router history={history}>
    <Switch>
      <CrumbRoute exact path={ROUTES.LOGIN} title="Login" component={Login} />
      <CrumbRoute exact path={ROUTES.RESET_PASSWORD} title="Reset Password" component={ResetPassword} />
      <Route path="/" component={ProtectedRoutes} />

      <Route component={NotFound} />
    </Switch>
  </Router>
)
