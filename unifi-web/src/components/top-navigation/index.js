import React, { Component } from 'react'
import get from 'lodash/get'
import { Menu } from '../../elements'

import './index.scss'

export default class TopNavigation extends Component {
  render() {
    const { onMenuClick, selectedKeys, verticalConfig } = this.props;
    const liveViewEnabled = get(verticalConfig, 'core.liveViewEnabled', false)
    const attendanceEnabled = Boolean(get(verticalConfig, 'attendance', false))

    return (
      <Menu
        className="top-navigation"
        onClick={onMenuClick}
        selectedKeys={selectedKeys}
        mode="horizontal"
        theme="dark"
      >
        <Menu.Item key="/directory">
          Directory
        </Menu.Item>

        {liveViewEnabled && <Menu.Item key="/live-view">
          Live View
        </Menu.Item>}

        {attendanceEnabled && <Menu.Item key="/attendance">
          Attendance
        </Menu.Item>}

        <Menu.Item key="/operators">
          Operators
        </Menu.Item>

        {/*<Menu.Item key="/">*/}
          {/*Sitemap*/}
        {/*</Menu.Item>*/}

        {/*<Menu.Item key="/navigation">*/}
          {/*Navigation*/}
        {/*</Menu.Item>*/}

        {/*<Menu.Item key="/site-manager">*/}
          {/*Site manager*/}
        {/*</Menu.Item>*/}

        {/*<Menu.Item key="/evacuation">*/}
          {/*Evacuation*/}
        {/*</Menu.Item>*/}

      </Menu>
    )
  }
}
