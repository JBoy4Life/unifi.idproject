import React from 'react'

import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { withRouter } from 'react-router'

import { actions as userActions, selectors as userSelectors } from 'reducers/user'
import { Breadcrumb, Layout, Aux } from 'elements'
import { IdentBar } from 'components'
import { LinkedNavigationMenu } from '../'

import './index.scss'


const { Header, Content } = Layout

const renderHeader = props => (
  <Aux>
    <Header className="header-ident">
      <IdentBar
        onLogout={props.logoutRequest}
        user={props.user}
      />
    </Header>
    <Header className="header-nav">
      <LinkedNavigationMenu />
    </Header>
  </Aux>
)

const PageContainer = (props) => {
  const { className = '', children, location } = props
  return (
    <Layout className={`${className} page-container`}>
      {location.pathname !== '/' && (
        <Breadcrumb data={{
          title: 'Home',
          pathname: '/'
        }} />
      )}
      {props.noHeader ? '' : renderHeader(props)}
      <Content className="main-content-container">
        {children}
      </Content>
    </Layout>
  )
}

const selector = createStructuredSelector({
  user: userSelectors.currentUserSelector,
})

const actions = {
  ...userActions
}

export default compose(
  withRouter,
  connect(selector, actions)
)(PageContainer)
