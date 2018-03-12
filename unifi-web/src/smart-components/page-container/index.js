import React, { Fragment } from 'react'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { withRouter } from 'react-router'

import { Layout } from 'elements'
import { NavBar } from '../'

import './index.scss'


const { Header, Content } = Layout

const renderHeader = () => (
  <Header className="header-nav">
    <NavBar />
  </Header>
)

const PageContainer = (props) => {
  const { className = '', children, location } = props
  return (
    <Layout className={`${className} page-container`}>
      {!props.noHeader && (
        <Header className="header-nav">
          <NavBar />
        </Header>
      )}
      <Content className="main-content-container">
        {children}
      </Content>
    </Layout>
  )
}

PageContainer.propTypes = {
  children: PropTypes.node,
  className: PropTypes.string,
  location: PropTypes.object.isRequired,
  noHeader: PropTypes.bool
}

export default withRouter(PageContainer)
