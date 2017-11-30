import React, { Component } from 'react'
import { Menu } from 'antd'

export default class TopNavigation extends Component {
  state = {
    current: 'mail',
  }

  handleClick = (e) => {
    this.setState({
      current: e.key,
    })
  }

  render() {
    return (
      <Menu
        onClick={this.handleClick}
        selectedKeys={[this.state.current]}
        mode="horizontal"
        theme="dark"
      >
        <Menu.Item key="directory">
          Directory
        </Menu.Item>

        <Menu.Item key="evacuation">
          Evacuation
        </Menu.Item>

        <Menu.Item key="live-view">
          Live View
        </Menu.Item>

        <Menu.Item key="navigation">
          Navigation
        </Menu.Item>

        <Menu.Item key="site-manager">
          Site manager
        </Menu.Item>

        <Menu.Item key="users">
          Users
        </Menu.Item>

      </Menu>
    )
  }
}
