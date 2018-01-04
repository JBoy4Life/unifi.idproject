import React, { Component } from 'react'
import PropTypes from 'prop-types'

import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'

import { Alert } from '../../elements'

import { PageContainer, LoginForm } from '../../smart-components'

import { noop } from '../../utils/helpers'

import './index.scss'
import unifilogo from '../../assets/images/unifi-logo.png'
import logo from '../../assets/images/ucl-logo-2.png'


import { actions as userActions } from '../../reducers/user'

class LoginContainer extends Component {
  static defaultProps = {
    loginRequest: noop,
  }

  static propTypes = {
    loginRequest: PropTypes.func,
  }

  handleLoginFormSubmit = data => this.props.loginRequest(data)

  render() {
    return (
      <PageContainer noHeader className="login-page">
        <div className="login-form-wrapper">
          <img className="logo" src={logo} alt="logo" />
          <div className="login-form-container">
            {this.props.user.error && (
            <Alert message={this.props.user.error} type="error" />
            )}
            <LoginForm onSubmit={this.handleLoginFormSubmit} />
          </div>
        </div>
      </PageContainer>
    )
  }
}

export const mapStateToProps = state => ({
  user: state.user,
})

export const mapDispatch = dispatch => (bindActionCreators({
  loginRequest: userActions.loginRequest,
}, dispatch))

export default connect(mapStateToProps, mapDispatch)(LoginContainer)
