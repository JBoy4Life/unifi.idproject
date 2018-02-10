import React from 'react'

import { Breadcrumbs, Layout, Aux } from 'elements'

import './index.scss'


const { Sider, Content } = Layout

export const Sidebar = ({ children }) => (<Aux>{children}</Aux>)

export const Main = ({ children }) => (<Aux>{children}</Aux>)

const PageContent = ({ children }) => (
  <Layout className="page-content-layout">
    {React.Children.map(children, (child) => {
      if (!child) {
        return child
      }
      if (child.type === Sidebar) {
        return <Sider className="page-sidebar" collapsedWidth="0" breakpoint="md">{child}</Sider>
      }

      return (
        <Content className="page-content">
          <Breadcrumbs className="page-content__crumbs" />
          {child}
        </Content>
      )
    })}
  </Layout>
)

PageContent.Main = Main
PageContent.Sidebar = Sidebar

export default PageContent
