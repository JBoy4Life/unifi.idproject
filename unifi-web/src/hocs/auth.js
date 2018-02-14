import React from 'react'
import get from 'lodash/get'
import locationHelperBuilder from 'redux-auth-wrapper/history4/locationHelper'
import { connectedRouterRedirect } from 'redux-auth-wrapper/history4/redirect'

import { loginStatusSelector, currentUserSelector, attendanceEnabledSelector, liveViewEnabledSelector } from 'redux/user/selectors'

const locationHelper = locationHelperBuilder({})

export const isLoggingIn = state => loginStatusSelector(state)

export const isLoggedIn = state => !!currentUserSelector(state)

const userIsAuthenticatedDefaults = {
  authenticatedSelector: isLoggedIn,
  authenticatingSelector: isLoggingIn,
  wrapperDisplayName: 'UserIsAuthenticated'
}

export const userIsAuthenticatedRedir = connectedRouterRedirect({
  ...userIsAuthenticatedDefaults,
  AuthenticatingComponent: () => (<div>Logging in...</div>),
  redirectPath: '/login'
})

const userIsNotAuthenticatedDefaults = {
  // Want to redirect the user when they are done LoggingIn and authenticated
  authenticatedSelector: state => !isLoggedIn(state) || !isLoggedIn(state),
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
