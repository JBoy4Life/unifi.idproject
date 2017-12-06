import React, { Component } from 'react'
import { Menu, Icon } from '../../elements'

export default class SideNavigation extends Component {
  renderMenuItem = ({ key, icon, label }) => (
    <Menu.Item key={key}>
      {icon && <Icon type={icon} />}
      {label}
    </Menu.Item>
  )

  render() {
    const { menus } = this.props
    return (
      <Menu
        onClick={this.props.onMenuClick}
        selectedKeys={this.props.selectedKeys}
      >
        {menus.map(this.renderMenuItem)}
      </Menu>
    )
  }
}
