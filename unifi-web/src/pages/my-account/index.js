import React, { Component } from 'react'

import { PageContainer } from 'smart-components'
import { userIsAuthenticatedRedir } from 'hocs/auth'

class MyAccount extends Component {
  render() {
    return (
      <PageContainer>
        <h1>My Account</h1>
      </PageContainer>
    )
  }
}

export default userIsAuthenticatedRedir(MyAccount)
