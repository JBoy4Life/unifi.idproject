import React, { Component } from 'react'
import { withRouter } from 'react-router-dom'

import { SideNavigation } from '../../components'

class LinkedTopNavigation extends Component {
  handleMenuNavigation = target => this.props.history.push(target.key)

  render() {
    const { location, menus } = this.props
    return (
      <SideNavigation
        menus={menus}
        selectedKeys={[location.pathname]}
        onMenuClick={this.handleMenuNavigation}
      />
    )
  }
}

export default withRouter(LinkedTopNavigation)
