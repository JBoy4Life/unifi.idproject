import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import withClientId from 'hocs/with-client-id'
import { API_FAIL, API_SUCCESS } from 'redux/api/request'
import { Col, Row } from 'elements'
import { formSubmit } from 'utils/form'
import { cancelPasswordRequest } from 'redux/user/actions'
import { PageContainer, ResetPasswordForm } from 'smart-components'
import { PageContent } from 'components'
import { cancelPasswordRequestStatus } from 'redux/user/selectors'
import './index.scss'

const COMPONENT_CSS_CLASS = 'reset-password'
const bemE = (suffix) => `${COMPONENT_CSS_CLASS}__${suffix}`

class CancelPasswordRequest extends Component {
  static propTypes = {
    cancelPasswordRequest: PropTypes.func.isRequired,
    history: PropTypes.object.isRequired,
    match: PropTypes.object,
    cancelPasswordRequestStatus: PropTypes.string,
    clientId: PropTypes.string
  }

  componentDidMount() {
    const { getPasswordResetInfo, clientId, match: { params: { username, token } } } = this.props
    getPasswordResetInfo({ clientId, username, token })
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
    const { passwordResetInfo } = this.props
    return (
      <PageContainer>
        <PageContent>
          <div>
            {API_SUCCESS === passwordResetInfo.status && (
              <Fragment>
                <Row>
                  <Col xs={24} sm={{ offset: 4}}>
                    <h1 className={bemE('title')}>
                      Welcome, {passwordResetInfo.operator.name || passwordResetInfo.operator.username}
                    </h1>
                  </Col>
                </Row>
                <Row>
                  <Col xs={24} sm={{ span: 8, offset: 4 }}>
                      <div>
                        <h2 className={bemE('body')}>Please choose a password.</h2>
                        <ResetPasswordForm onSubmit={this.handleResetPasswordFormSubmit} />
                      </div>
                  </Col>
                </Row>
              </Fragment>
            )}
            {API_FAIL === passwordResetInfo.status && (
              <h1 className={bemE('body')}>Invalid username or token.</h1>
            )}
          </div>
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
)(CancelPasswordRequest)
