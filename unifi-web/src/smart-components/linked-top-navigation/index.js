import React, { Component } from 'react'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { withRouter } from 'react-router-dom'

import { verticalConfigSelector } from 'redux/selectors'
import { TopNavigation } from 'components'
import { userIsAuthenticated } from 'hocs/auth'

class LinkedTopNavigation extends Component {
  handleMenuNavigation = target => this.props.history.push(target.key)

  render() {
    const { verticalConfig, match } = this.props

    return (
      <TopNavigation
        verticalConfig={verticalConfig}
        selectedKeys={[match.path]}
        onMenuClick={this.handleMenuNavigation}
      />
    )
  }
}

const selector = createStructuredSelector({
  verticalConfig: verticalConfigSelector
})

export default compose(
  userIsAuthenticated,
  withRouter,
  connect(selector)
)(LinkedTopNavigation)
