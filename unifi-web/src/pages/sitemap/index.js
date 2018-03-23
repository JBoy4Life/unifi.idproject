import React, { Component } from 'react'
import { Link } from 'react-router-dom'

import * as ROUTES from 'config/routes'
import { PageContent } from '../../components'
import { PageContainer } from '../../smart-components'

export default class SitemapContainer extends Component {
  render() {
    return (
      <PageContainer>
        <PageContent>
          <PageContent.Main>
            <h1>Sitemap</h1>
            <ul>
              <li><Link to={ROUTES.ATTENDANCE_SCHEDULES}>Attendance</Link></li>
              <li><Link to={ROUTES.LOGIN}>Login</Link></li>
              {/*<li><Link to={ROUTES.MY_ACCOUNT}>My account</Link></li>*/}
              {/*<li><Link to={ROUTES.CLIENT_REGISTRY}>Client registry</Link></li>*/}
            </ul>
          </PageContent.Main>
        </PageContent>
      </PageContainer>
    )
  }
}

