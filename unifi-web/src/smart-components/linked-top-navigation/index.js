import React, { Component } from 'react'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { withRouter } from 'react-router-dom'

import { liveViewEnabledSelector } from 'redux/user/selectors'
import { TopNavigation } from '../../components'

class LinkedTopNavigation extends Component {
  handleMenuNavigation = target => this.props.history.push(target.key)

  render() {
    const { liveViewEnabled, match } = this.props

    return (
      <TopNavigation
        liveViewEnabled={liveViewEnabled}
        selectedKeys={[match.path]}
        onMenuClick={this.handleMenuNavigation}
      />
    )
  }
}

const selector = createStructuredSelector({
  liveViewEnabled: liveViewEnabledSelector
})

export default compose(
  withRouter,
  connect(selector)
)(LinkedTopNavigation)
