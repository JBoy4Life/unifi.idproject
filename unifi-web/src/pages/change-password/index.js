import React, { Component, Fragment } from 'react'
import pick from 'lodash/pick'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link } from 'react-router-dom'

import { API_SUCCESS } from 'redux/api/request'
import { Col, Row } from 'elements'
import { formSubmit } from 'utils/form'
import { changePassword } from 'redux/user/actions'
import { PageContainer, ChangePasswordForm } from 'smart-components'
import { PageContent } from 'components'
import { changePasswordStatusSelector } from 'redux/user/selectors'
import { userIsAuthenticatedRedir } from 'hocs/auth'
import './index.scss'

const COMPONENT_CSS_CLASS = 'change-password'
const bemE = (suffix) => `${COMPONENT_CSS_CLASS}__${suffix}`

class ChangePassword extends Component {
  static propTypes = {
    changePassword: PropTypes.func,
    changePasswordStatus: PropTypes.string
  }

  componentWillReceiveProps(nextProps) {
    const { history } = this.props
    if (this.props.changePasswordStatus !== nextProps.changePasswordStatus &&
      nextProps.changePasswordStatus === API_SUCCESS) {
      history.push('/')
    }
  }

  handleChangePasswordFormSubmit = (data) => {
    const { clientId, changePassword } = this.props
    return formSubmit(changePassword, pick(data, ['currentPassword', 'password']))
  }

  render() {
    return (
      <PageContainer>
        <PageContent>
          <PageContent.Main>
            <p className={bemE('back')}><Link to="/">&laquo; Back</Link></p>
            <Row>
              <Col xs={24} sm={14} md={12} xl={10}>
                <h1 className={bemE('title')}>Change my password</h1>
                <ChangePasswordForm onSubmit={this.handleChangePasswordFormSubmit} />
              </Col>
            </Row>
          </PageContent.Main>
        </PageContent>
      </PageContainer>
    )
  }
}

export const selector = createStructuredSelector({
  changePasswordStatus: changePasswordStatusSelector
})

export const actions = {
  changePassword
}

export default compose(
  userIsAuthenticatedRedir,
  connect(selector, actions)
)(ChangePassword)
