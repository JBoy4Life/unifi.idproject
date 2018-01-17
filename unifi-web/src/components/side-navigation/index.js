import React, { Component } from 'react'
import { Menu, Icon } from '../../elements'

import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'side-nav'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

export default class SideNavigation extends Component {
  renderMenuItem = ({ key, icon, label }) => (
    <Menu.Item key={key}>
      {icon && <Icon type={icon} className={bemE('icon')} />}
      {label}
    </Menu.Item>
  )

  render() {
    const { menus } = this.props
    return (
      <Menu
        onClick={this.props.onMenuClick}
        selectedKeys={this.props.selectedKeys}
        className={COMPONENT_CSS_CLASSNAME}
      >
        {menus.map(this.renderMenuItem)}
      </Menu>
    )
  }
}
