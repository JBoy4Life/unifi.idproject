import React, { Component } from 'react'
import { withRouter } from 'react-router-dom'

import { TopNavigation } from '../../components'

class LinkedTopNavigation extends Component {
  handleMenuNavigation = target => this.props.history.push(target.key)

  render() {
    const { match } = this.props

    return (
      <TopNavigation
        selectedKeys={[match.path]}
        onMenuClick={this.handleMenuNavigation}
      />
    )
  }
}

export default withRouter(LinkedTopNavigation)
