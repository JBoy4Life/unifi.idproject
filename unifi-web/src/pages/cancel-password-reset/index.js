import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import withClientId from 'hocs/with-client-id'
import { API_FAIL, API_PENDING, API_SUCCESS } from 'redux/api/request'
import { Col, Row } from 'elements'
import { formSubmit } from 'utils/form'
import { cancelPasswordReset } from 'redux/user/actions'
import { PageContainer } from 'smart-components'
import { PageContent } from 'components'
import { cancelPasswordResetStatusSelector } from 'redux/user/selectors'
import './index.scss'

const COMPONENT_CSS_CLASS = 'cancel-password-reset'
const bemE = (suffix) => `${COMPONENT_CSS_CLASS}__${suffix}`

class CancelPasswordReset extends Component {
  static propTypes = {
    cancelPasswordReset: PropTypes.func.isRequired,
    cancelPasswordResetStatus: PropTypes.string,
    clientId: PropTypes.string,
    history: PropTypes.object.isRequired,
    match: PropTypes.object
  }

  componentWillMount() {
    const { cancelPasswordReset, clientId, match: { params: { username, token } } } = this.props
    cancelPasswordReset({ clientId, username, token })
  }

  render() {
    const { cancelPasswordResetStatus } = this.props
    return (
      <PageContainer>
        <PageContent>
          {API_SUCCESS === cancelPasswordResetStatus && (
            <h1 className={bemE('message')}>Password reset request has been cancelled</h1>
          )}
          {API_FAIL === cancelPasswordResetStatus && (
            <h1 className={bemE('message')}>Invalid username or token.</h1>
          )}
          {API_PENDING === cancelPasswordResetStatus && (
            <h1 className={bemE('message')}>Loading...</h1>
          )}
        </PageContent>
      </PageContainer>
    )
  }
}

export const selector = createStructuredSelector({
  cancelPasswordResetStatus: cancelPasswordResetStatusSelector
})

export const actions = {
  cancelPasswordReset
}

export default compose(
  withClientId,
  connect(selector, actions)
)(CancelPasswordReset)
