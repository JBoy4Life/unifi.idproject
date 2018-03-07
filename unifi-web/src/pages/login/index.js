import React, { Component } from 'react'
import get from 'lodash/get'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import './index.scss'
import logo from 'assets/images/ucl-logo-2.png'
import unifilogo from 'assets/images/unifi-logo.svg'
import { actions as userActions } from 'redux/user'
import { currentClientSelector } from 'redux/clients/selectors'
import { formSubmit } from 'utils/form'
import { noop } from 'utils/helpers'
import { PageContainer, LoginForm } from 'smart-components'
import { PageContent } from 'components'
import { userIsNotAuthenticatedRedir } from 'hocs/auth'

const COMPONENT_CSS_CLASSNAME = 'login-page'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

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
    const { client } = this.props
    const image = get(client, 'image')
    return (
      <PageContainer className={COMPONENT_CSS_CLASSNAME}>
        <PageContent>
          <PageContent.Main>
            <div className={bemE('form')}>
              {image && (
                <img className={bemE('logo')} src={`data:${image.mimeType};base64,${image.data}`} alt={client.displayName} />
              )}
              <h1 className={bemE('title')}>Sign In</h1>
              <LoginForm onSubmit={this.handleLoginFormSubmit} />
            </div>
          </PageContent.Main>
        </PageContent>
      </PageContainer>
    )
  }
}

const selector = createStructuredSelector({
  client: currentClientSelector
})

export const actions = {
  loginRequest: userActions.loginRequest,
}

export default compose(
  userIsNotAuthenticatedRedir,
  connect(selector, actions)
)(LoginContainer)
