import React, { Component } from 'react'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import Loading from 'components/loading'
import Routes from './routes'
import { getReducer as userSelector } from 'redux/user/selectors'
import { reauthenticateRequest, setInitialized } from 'redux/user/actions'

class Main extends Component {
  componentWillMount() {
    const { reauthenticateRequest, setInitialized, user: { currentUser } } = this.props;
    // Reauthenticate if we have a session.
    if (currentUser && currentUser.token) {
      reauthenticateRequest(currentUser.token)
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
  reauthenticateRequest,
  setInitialized
}

export default connect(selector, actions)(Main)

