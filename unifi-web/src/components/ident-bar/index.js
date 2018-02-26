import React, { Component } from 'react'
import cn from 'classnames'
import { withRouter } from 'react-router'

import logo from 'assets/images/ucl-logo.svg'
import { Collapse, Dropdown, Menu, Icon } from 'elements'

import './index.scss'

const COMPONENT_CSS_CLASS = 'ident-bar'
const bemE = (suffix) => `${COMPONENT_CSS_CLASS}__${suffix}`

class IdentBar extends Component {
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
        <img className={bemE('logo')} src={logo} alt="logo" />
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
      </div>
    )
  }
}

export default withRouter(IdentBar)
