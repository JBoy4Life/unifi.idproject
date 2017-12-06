import React from 'react'

import { Row, Col } from '../../elements'

export const Sidebar = ({ children }) => (
  <div className="page-sidebar">
    {children}
  </div>
)

export const Main = ({ children }) => (
  <div className="page-content">
    {children}
  </div>
)

const hasSidebar = (children) => {
  let found = false
  React.Children
    .forEach(children, (child) => {
      if (child.type === Sidebar) {
        found = true
      }
    })
  return found
}

const PageContent = ({ children }) => {
  const contentSpan = hasSidebar(children) ? 21 : 24
  return (
    <Row className="page-content" gutter={16}>
      {React.Children.map(children, (child) => {
      if (child.type === Sidebar) {
        return <Col span={3}>{child}</Col>
      }

      return <Col span={contentSpan}>{child}</Col>
    })}
    </Row>
  )
}

PageContent.Main = Main
PageContent.Sidebar = Sidebar

export default PageContent
