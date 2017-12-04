import React, { Component } from 'react'

import { PageContainer } from '../../components'
import { LoginForm } from '../../smart-components'

export default class SitemapContainer extends Component {
  render() {
    return (
      <PageContainer>
        <h1>Login</h1>
        <LoginForm />
      </PageContainer>
    )
  }
}
