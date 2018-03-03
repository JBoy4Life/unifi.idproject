import React from 'react'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { withRouter } from 'react-router'

import { Layout, Aux } from 'elements'
import { NavBar } from '../'

import './index.scss'


const { Header, Content } = Layout

const renderHeader = () => (
  <Aux>
    <Header className="header-nav">
      <NavBar />
    </Header>
  </Aux>
)

const PageContainer = (props) => {
  const { className = '', children, location } = props
  return (
    <Layout className={`${className} page-container`}>
      {!props.noHeader && (
        <Aux>
          <Header className="header-nav">
            <NavBar />
          </Header>
        </Aux>
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
