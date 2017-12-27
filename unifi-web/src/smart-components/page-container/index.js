import React from 'react'

import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'

import { actions as userActions, selectors as userSelectors } from '../../reducers/user'

import { IdentBar } from '../../components'
import { LinkedNavigationMenu } from '../'
import { Layout, Aux } from '../../elements'

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
  const { className = '', children } = props
  return (
    <Layout className={`${className} page-contianer`}>
      {props.noHeader ? '' : renderHeader(props)}
      <Content className="main-content-container">
        {children}
      </Content>
    </Layout>
  )
}

const mapStateToProps = state => ({
  user: userSelectors.getCurrentUser(state),
})
const mapDispatchToProps = d => bindActionCreators(userActions, d)


export default connect(mapStateToProps, mapDispatchToProps)(PageContainer)

