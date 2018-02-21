import React, { Component } from 'react'
import { Route, Switch /* Redirect */ } from 'react-router'

import * as ROUTES from 'utils/routes'
import Dashboard from './dashboard'
import DirectoryView from './directory'
import FloorView from './floor-view'
import { liveViewEnabledRedir } from 'hocs/auth'
import { PageContainer, LinkedSideNavigation } from 'smart-components'
import { PageContent } from 'components'

const menus = [
  {
    key: ROUTES.LIVE_VIEW,
    label: 'Dashboard',
  },
  {
    key: ROUTES.LIVE_VIEW_DIRECTORY,
    label: 'Directory',
  },
  {
    key: ROUTES.LIVE_VIEW_FLOOR_VIEW,
    label: 'Floor view',
  },
]

class LiveViewContainer extends Component {
  render() {
    return (
      <PageContainer>
        <PageContent>
          <PageContent.Main>
            <DirectoryView />
          </PageContent.Main>
        </PageContent>
      </PageContainer>
    )
  }
}

export default liveViewEnabledRedir(LiveViewContainer)
