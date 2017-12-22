import React from 'react'

import { IdentBar } from '../../components'
import { LinkedNavigationMenu } from '../'
import { Layout } from '../../elements'

import './index.scss'


const { Header, Content, Footer } = Layout

const PageContainer = ({ className = '', children }) => (
  <Layout className={`${className} page-contianer`}>
    <Header className="header-ident">
      <IdentBar />
    </Header>
    <Header className="header-nav">
      <LinkedNavigationMenu />
    </Header>
    <Content className="main-content-container">
      {children}
    </Content>
    <Footer style={{ textAlign: 'center' }}>
      Copyright © Unifi.id, 2017
    </Footer>
  </Layout>
)

export default PageContainer
