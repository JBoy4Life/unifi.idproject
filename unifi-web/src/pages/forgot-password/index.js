import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import withClientId from 'hocs/with-client-id'
import { API_FAIL, API_SUCCESS } from 'redux/api/constants'
import { Col, Row } from 'elements'
import { formSubmit } from 'utils/form'
import { PageContainer, ForgotPasswordForm } from 'smart-components'
import { PageContent } from 'components'
import { requestPasswordReset } from 'redux/modules/user'
import { requestPasswordResetStatusSelector } from 'redux/selectors'

class ForgotPassword extends Component {
  static propTypes = {
    clientId: PropTypes.string.isRequired,
    history: PropTypes.object.isRequired,
    match: PropTypes.object.isRequired,
    requestPasswordReset: PropTypes.func.isRequired
  }

  handleForgotPasswordFormSubmit = (data) => {
    const { clientId, requestPasswordReset } = this.props
    return formSubmit(requestPasswordReset, {
      clientId,
      username: data.username
    })
  }

  render() {
    const { history, passwordResetInfo } = this.props
    return (
      <PageContainer>
        <PageContent>
          <Row>
            <Col xs={24} sm={{ offset: 4, span: 16 }} md={{ offset: 6, span: 12 }} xl={{ offset: 7, span: 10 }}>
              <h1>Forgotten password</h1>
              <ForgotPasswordForm onSubmit={this.handleForgotPasswordFormSubmit} history={history} />
            </Col>
          </Row>
        </PageContent>
      </PageContainer>
    )
  }
}

export const selector = createStructuredSelector({
  requestPasswordResetStatus: requestPasswordResetStatusSelector
})

export const actions = {
  requestPasswordReset
}

export default compose(
  withClientId,
  connect(selector, actions)
)(ForgotPassword)
