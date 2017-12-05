import React, { Component } from 'react'
import PropTypes from 'prop-types'

import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'

import { PageContainer } from '../../components'
import { LoginForm } from '../../smart-components'

import { noop } from '../../utils/helpers'

import './index.scss'


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
      <PageContainer className="login-page">
        <h1>Login</h1>
        <div className="login-form-container">
          <LoginForm onSubmit={this.handleLoginFormSubmit} />
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
