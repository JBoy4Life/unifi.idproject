import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import './index.scss'
import logo from 'assets/images/ucl-logo-2.png'
import unifilogo from 'assets/images/unifi-logo.svg'
import { actions as userActions } from 'redux/user'
import { formSubmit } from 'utils/form'
import { noop } from 'utils/helpers'
import { PageContainer, LoginForm } from 'smart-components'
import { userIsNotAuthenticatedRedir } from 'hocs/auth'

class LoginContainer extends Component {
  static defaultProps = {
    loginRequest: noop,
  }

  static propTypes = {
    loginRequest: PropTypes.func,
  }

  handleLoginFormSubmit = (data) => {
    return formSubmit(this.props.loginRequest, data)
  }

  render() {
    return (
      <PageContainer noHeader className="login-page">
        <div className="login-form-wrapper">
          <img className="logo" src={logo} alt="logo" />
          <div className="login-form-container">
            <LoginForm onSubmit={this.handleLoginFormSubmit} />
          </div>
        </div>
      </PageContainer>
    )
  }
}

export const actions = {
  loginRequest: userActions.loginRequest,
}

export default compose(
  userIsNotAuthenticatedRedir,
  connect(null, actions)
)(LoginContainer)
