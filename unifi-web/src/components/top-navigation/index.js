import React, { Component } from 'react'
import Menu from 'antd/lib/menu'
import 'antd/lib/menu/style/index.css'

export default class TopNavigation extends Component {
  render() {
    return (
      <Menu
        onClick={this.props.onMenuClick}
        selectedKeys={this.props.selectedKeys}
        mode="horizontal"
        theme="dark"
      >
        <Menu.Item key="/">
          Sitemap
        </Menu.Item>
        <Menu.Item key="/directory">
          Directory
        </Menu.Item>

        <Menu.Item key="/evacuation">
          Evacuation
        </Menu.Item>

        <Menu.Item key="/live-view">
          Live View
        </Menu.Item>

        <Menu.Item key="/navigation">
          Navigation
        </Menu.Item>

        <Menu.Item key="/site-manager">
          Site manager
        </Menu.Item>

        <Menu.Item key="/users">
          Users
        </Menu.Item>

      </Menu>
    )
  }
}
