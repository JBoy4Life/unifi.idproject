import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import * as ROUTES from 'utils/routes'
import withClientId from 'hocs/with-client-id'
import { API_FAIL, API_SUCCESS } from 'redux/api/request'
import { Col, Row } from 'elements'
import { formSubmit } from 'utils/form'
import { getPasswordResetInfo, setPassword } from 'redux/user/actions'
import { PageContainer, ResetPasswordForm } from 'smart-components'
import { PageContent } from 'components'
import { passwordResetInfoSelector, setPasswordStatusSelector } from 'redux/user/selectors'
import './index.scss'

const COMPONENT_CSS_CLASS = 'reset-password'
const bemE = (suffix) => `${COMPONENT_CSS_CLASS}__${suffix}`

class ResetPassword extends Component {
  static propTypes = {
    getPasswordResetInfo: PropTypes.func,
    match: PropTypes.object,
    passwordResetInfo: PropTypes.object,
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
    const { match, passwordResetInfo } = this.props

    return (
      <PageContainer>
        <PageContent>
          {API_SUCCESS === passwordResetInfo.status && (
            <Row>
              <Col xs={24} sm={{ offset: 4, span: 16 }} md={{ offset: 6, span: 12 }} xl={{ offset: 7, span: 10 }}>
                {match.path === ROUTES.ACCEPT_INVITATION ? (
                  <Fragment>
                    <h1 className={bemE('title')}>Welcome</h1>
                    <p className={bemE('body')}>To join unifi.id please create a password.</p>
                  </Fragment>
                ) : (
                  <Fragment>
                    <h1 className={bemE('title')}>Welcome</h1>
                    <p className={bemE('body')}>Please enter a password.</p>
                  </Fragment>
                )}
                <ResetPasswordForm onSubmit={this.handleResetPasswordFormSubmit} />
              </Col>
            </Row>
          )}
          {API_FAIL === passwordResetInfo.status && (
            <h1 className={bemE('title')}>Invalid username or token.</h1>
          )}
        </PageContent>
      </PageContainer>
    )
  }
}

export const selector = createStructuredSelector({
  passwordResetInfo: passwordResetInfoSelector,
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
