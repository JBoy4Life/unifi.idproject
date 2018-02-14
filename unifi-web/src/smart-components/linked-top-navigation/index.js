import React, { Component } from 'react'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { withRouter } from 'react-router-dom'

import { verticalConfigSelector } from 'redux/user/selectors'
import { TopNavigation } from '../../components'

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
  withRouter,
  connect(selector)
)(LinkedTopNavigation)
