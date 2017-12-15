import React from 'react'

import { LinkedNavigationMenu } from '../'
import { Layout } from '../../elements'

import './index.scss'


const { Header, Content, Footer } = Layout

const PageContainer = ({ className = '', children }) => (
  <Layout className={`${className} page-contianer`}>
    <Header>
      <LinkedNavigationMenu />
    </Header>
    <Content className="main-content-container">
      {children}
    </Content>
    <Footer style={{ textAlign: 'center' }}>
      ©2017
    </Footer>
  </Layout>
)

export default PageContainer
