import React from 'react'
import get from 'lodash/get'
import connectedAuthWrapper from 'redux-auth-wrapper/connectedAuthWrapper'
import locationHelperBuilder from 'redux-auth-wrapper/history4/locationHelper'
import { connectedRouterRedirect } from 'redux-auth-wrapper/history4/redirect'

import * as ROUTES from 'utils/routes'
import { clientIsLoadingSelector, clientIsLoadedSelector } from 'redux/clients/selectors'
import { Loading } from 'components'
import { loginStatusSelector, currentUserSelector, attendanceEnabledSelector, liveViewEnabledSelector } from 'redux/user/selectors'

const locationHelper = locationHelperBuilder({})

export const isLoggingIn = state => loginStatusSelector(state)

export const isLoggedIn = state => !!currentUserSelector(state)

const userIsAuthenticatedDefaults = {
  authenticatedSelector: isLoggedIn,
  authenticatingSelector: isLoggingIn,
  wrapperDisplayName: 'UserIsAuthenticated'
}

export const userIsAuthenticated = connectedAuthWrapper(userIsAuthenticatedDefaults)

export const userIsAuthenticatedRedir = connectedRouterRedirect({
  ...userIsAuthenticatedDefaults,
  AuthenticatingComponent: () => (<Loading />),
  redirectPath: '/login'
})

const userIsNotAuthenticatedDefaults = {
  // Want to redirect the user when they are done LoggingIn and authenticated
  authenticatedSelector: state => !isLoggedIn(state),
  wrapperDisplayName: 'UserIsNotAuthenticated'
}

export const userIsNotAuthenticatedRedir = connectedRouterRedirect({
  ...userIsNotAuthenticatedDefaults,
  redirectPath: (state, ownProps) => locationHelper.getRedirectQueryParam(ownProps) || '/attendance', // TODO: replace with dashboard path
  allowRedirectBack: false
})

export const attendanceEnabledRedir = connectedRouterRedirect({
  authenticatedSelector: attendanceEnabledSelector,
  wrapperDisplayName: 'AttendanceEnabled',
  redirectPath: (state, ownProps) => locationHelper.getRedirectQueryParam(ownProps) || '/live-view', // TODO: replace with dashboard path
  allowRedirectBack: false
})

export const liveViewEnabledRedir = connectedRouterRedirect({
  authenticatedSelector: liveViewEnabledSelector,
  wrapperDisplayName: 'LiveViewEnabled',
  redirectPath: (state, ownProps) => locationHelper.getRedirectQueryParam(ownProps) || '/attendance', // TODO: replace with dashboard path
  allowRedirectBack: false
})

export const clientIsValidRedir = connectedRouterRedirect({
  authenticatedSelector: clientIsLoadedSelector,
  authenticatingSelector: clientIsLoadingSelector,
  wrapperDisplayName: 'ClientIsValid',
  AuthenticatingComponent: () => (<Loading />),
  redirectPath: ROUTES.NOT_FOUND,
  allowRedirectBack: false
})
