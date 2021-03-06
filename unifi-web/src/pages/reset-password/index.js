import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import * as ROUTES from 'config/routes'
import withClientId from 'hocs/with-client-id'
import { API_FAIL, API_PENDING, API_SUCCESS } from 'redux/api/constants'
import { Col, Row } from 'elements'
import { formSubmit } from 'utils/form'
import { getPasswordResetInfo, setPassword } from 'redux/modules/user'
import { PageContainer, ResetPasswordForm } from 'smart-components'
import { PageContent } from 'components'
import {
  passwordResetInfoSelector,
  passwordResetInfoStatusSelector,
  setPasswordStatusSelector
} from 'redux/selectors'
import './index.scss'

const COMPONENT_CSS_CLASS = 'reset-password'
const bemE = (suffix) => `${COMPONENT_CSS_CLASS}__${suffix}`

class ResetPassword extends Component {
  static propTypes = {
    getPasswordResetInfo: PropTypes.func,
    match: PropTypes.object,
    passwordResetInfo: PropTypes.object,
    passwordResetInfoStatus: PropTypes.string,
    setPassword: PropTypes.func,
    setPasswordStatus: PropTypes.string,
    clientId: PropTypes.string
  }

  componentDidMount() {
    const { getPasswordResetInfo, clientId, match: { params: { username, token } } } = this.props
    getPasswordResetInfo({ clientId, username, token })
  }

  componentWillReceiveProps(nextProps) {
    const { history } = this.props
    if (this.props.setPasswordStatus !== nextProps.setPasswordStatus &&
      nextProps.setPasswordStatus === API_SUCCESS) {
      history.push('/')
    }
  }

  handleResetPasswordFormSubmit = (data) => {
    const { clientId, match: { params: { username, token } }, setPassword } = this.props
    return formSubmit(setPassword, {
      clientId,
      username,
      token,
      password: data.password
    })
  }

  render() {
    const { match, passwordResetInfo, passwordResetInfoStatus } = this.props

    return (
      <PageContainer>
        <PageContent>
          {API_SUCCESS === passwordResetInfoStatus && passwordResetInfo && (
            <Row>
              <Col xs={24} sm={{ offset: 4, span: 16 }} md={{ offset: 6, span: 12 }} xl={{ offset: 7, span: 10 }}>
                {match.path === ROUTES.ACCEPT_INVITATION ? (
                  <Fragment>
                    <h1 className={bemE('title')}>Welcome, {passwordResetInfo.operator.name}</h1>
                    <p className={bemE('body')}>To join unifi.id please create a password.</p>
                  </Fragment>
                ) : (
                  <Fragment>
                    <h1 className={bemE('title')}>Reset password</h1>
                    <p className={bemE('body')}>Please enter a password.</p>
                  </Fragment>
                )}
                <ResetPasswordForm onSubmit={this.handleResetPasswordFormSubmit} />
              </Col>
            </Row>
          )}
          {API_PENDING !== passwordResetInfoStatus && !passwordResetInfo && (
            <h1 className={bemE('title')}>Invalid username or token.</h1>
          )}
        </PageContent>
      </PageContainer>
    )
  }
}

export const selector = createStructuredSelector({
  passwordResetInfo: passwordResetInfoSelector,
  passwordResetInfoStatus: passwordResetInfoStatusSelector,
  setPasswordStatus: setPasswordStatusSelector
})

export const actions = {
  getPasswordResetInfo,
  setPassword
}

export default compose(
  withClientId,
  connect(selector, actions)
)(ResetPassword)
