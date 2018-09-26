import React, { Fragment } from 'react'

import { Container, Layout } from 'elements'

import './index.scss'


const { Sider, Content } = Layout

export const Sidebar = ({ children }) => (<Fragment>{children}</Fragment>)

export const Main = ({ children }) => (<Fragment>{children}</Fragment>)

const PageContent = ({ children, className }) => (
  <Container className={`${className} page-content-layout`} tag={Layout}>
    {React.Children.map(children, (child) => {
      if (!child) {
        return child
      }
      if (child.type === Sidebar) {
        return <Sider className="page-sidebar" collapsedWidth="0" breakpoint="md">{child}</Sider>
      }

      return (
        <Content>
          {child}
        </Content>
      )
    })}
  </Container>
)

PageContent.Main = Main
PageContent.Sidebar = Sidebar

export default PageContent
