import React, { Component } from 'react'
import { compose } from 'redux'
import get from 'lodash/get'
import { Menu } from '../../elements'
import { TESTING_CLIENT_IDS } from '../../config/constants'
import { withClientId } from 'hocs'

import './index.scss'

class TopNavigation extends Component {
  render() {
    const { clientId, onMenuClick, selectedKeys, verticalConfig } = this.props;
    const liveViewEnabled = get(verticalConfig, 'core.liveViewEnabled', false)
    const attendanceEnabled = Boolean(get(verticalConfig, 'attendance', false))
    const evacDemoEnabled = (TESTING_CLIENT_IDS.includes(clientId))

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

        <Menu.Item key="/reports">
          Reports
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

        {evacDemoEnabled && <Menu.Item key="/evacuation-demo">
          Evacuation
        </Menu.Item>}

      </Menu>
    )
  }
}

export default compose(
  withClientId,
)(TopNavigation)
