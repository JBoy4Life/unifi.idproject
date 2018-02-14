import React, { Component } from 'react'
import get from 'lodash/get'
import { Menu } from '../../elements'

import './index.scss'

export default class TopNavigation extends Component {
  render() {
    const { verticalConfig } = this.props;
    const liveViewEnabled = get(verticalConfig, 'core.liveViewEnabled', false)
    const attendanceEnabled = Boolean(get(verticalConfig, 'attendance', false))
    return (
      <Menu
        className="top-navigation"
        onClick={this.props.onMenuClick}
        selectedKeys={this.props.selectedKeys}
        mode="horizontal"
        theme="dark"
      >
        {/*<Menu.Item key="/">*/}
          {/*Sitemap*/}
        {/*</Menu.Item>*/}

        {/*<Menu.Item key="/directory">*/}
          {/*Directory*/}
        {/*</Menu.Item>*/}

        {liveViewEnabled && <Menu.Item key="/live-view">
          Live View
        </Menu.Item>}

        {/*<Menu.Item key="/navigation">*/}
          {/*Navigation*/}
        {/*</Menu.Item>*/}

        {/*<Menu.Item key="/site-manager">*/}
          {/*Site manager*/}
        {/*</Menu.Item>*/}

        {/*<Menu.Item key="/users">*/}
          {/*Users*/}
        {/*</Menu.Item>*/}

        {/*<Menu.Item key="/evacuation">*/}
          {/*Evacuation*/}
        {/*</Menu.Item>*/}

        {attendanceEnabled && <Menu.Item key="/attendance/schedules">
          Attendance
        </Menu.Item>}

      </Menu>
    )
  }
}
