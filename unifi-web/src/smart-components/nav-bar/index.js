import React, { Component } from 'react'
import cn from 'classnames'
import { withRouter } from 'react-router'

import logo from 'assets/images/unifi-logo.svg'
import { Collapse, Container, Dropdown, Menu, Icon } from 'elements'
import { LinkedNavigationMenu } from '../'

import './index.scss'

const COMPONENT_CSS_CLASS = 'nav-bar'
const bemE = (suffix) => `${COMPONENT_CSS_CLASS}__${suffix}`

class NavBar extends Component {
  constructor(props) {
    super(props)

    this.state = {
      menuOpen: false
    }
  }

  componentDidMount() {
    window.addEventListener('click', this.handleHideMenu)
  }

  componentWillUnmount() {
    window.removeEventListener('click', this.handleHideMenu)
  }

  handleMenuItemClick = ({ key }) => {
    const { history, onLogout } = this.props
    if (key === 'logout') {
      onLogout()
    } else {
      history.push(`/${key}`)
    }
  }

  handleHideMenu = (event) => {
    event.stopPropagation()
    this.setState({ menuOpen: false })
  }

  toggleMenuOpen = (event) => {
    const { menuOpen } = this.state
    event.stopPropagation()
    this.setState({
      menuOpen: !menuOpen
    })
  }

  render() {
    const { user } = this.props
    const { menuOpen } = this.state

    return (
      <div className={COMPONENT_CSS_CLASS}>
        <Container className={bemE('container')}>
          <img className={bemE('logo')} src={logo} alt="logo" />
          <LinkedNavigationMenu />
          {user && (
            <div className={bemE('user-content')}>
              <button
                className={cn(bemE('menu-toggle'), {
                  [bemE('menu-toggle--open')]: menuOpen
                })}
                onClick={this.toggleMenuOpen}
              >
                <span className={bemE('username')}>
                  {user.operator.username}
                </span>
                <Icon type={menuOpen ? 'caret-up' : 'caret-down'} className={bemE('caret')} />
              </button>
              <Menu
                onClick={this.handleMenuItemClick}
                className={cn(bemE('menu'), {
                  [bemE('menu--open')]: menuOpen
                })}
                selectedKeys={[]}
              >
                <Menu.Item key="change-password">Change my password</Menu.Item>
                <Menu.Divider />
                <Menu.Item key="logout">Log out</Menu.Item>
              </Menu>
            </div>
          )}
        </Container>
      </div>
    )
  }
}

export default withRouter(NavBar)
