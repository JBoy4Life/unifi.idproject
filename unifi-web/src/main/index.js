import React, { Component } from 'react'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import Loading from 'components/loading'
import Routes from './routes'
import withClientId from 'hocs/with-client-id'
import { getClient } from 'redux/modules/client/actions'
import { getReducer as userSelector } from 'redux/modules/user/selectors'
import { reauthenticateRequest, setInitialized } from 'redux/modules/user/actions'

class Main extends Component {
  componentWillMount() {
    const { clientId, getClient, reauthenticateRequest, setInitialized, user: { currentUser } } = this.props;
    // Reauthenticate if we have a session.
    getClient({ clientId })
    if (currentUser && currentUser.token) {
      reauthenticateRequest({ clientId, sessionToken: currentUser.token })
    } else {
      setInitialized()
    }
  }

  render() {
    const { user, history } = this.props
    return user.initialising ? (
      <Loading />
    ) : (
      <Routes history={history} />
    )
  }
}

const selector = createStructuredSelector({
  user: userSelector
})

const actions = {
  getClient,
  reauthenticateRequest,
  setInitialized
}

export default compose(
  withClientId, 
  connect(selector, actions)
)(Main)
