import React, { Component } from 'react'
import { Link } from 'react-router-dom'

import * as ROUTES from '../../utils/routes'
import { PageContainer } from '../../components'


export default class SitemapContainer extends Component {
  render() {
    return (
      <PageContainer>
        <h1>Sitemap</h1>
        <ul>
          <li><Link to={ROUTES.LOGIN}>Login</Link></li>
          <li><Link to={ROUTES.MY_ACCOUNT}>My account</Link></li>
        </ul>
      </PageContainer>
    )
  }
}
