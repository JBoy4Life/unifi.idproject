import React, { Component } from 'react'
import cn from 'classnames'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { withRouter } from 'react-router'

import * as ROUTES from 'utils/routes'
import { actions as userActions, selectors as userSelectors } from 'redux/user'
import { Menu, Icon } from 'elements'
import { userIsAuthenticated } from 'hocs/auth'
import './index.scss'

const COMPONENT_CSS_CLASS = 'user-actions'
const bemE = (suffix) => `${COMPONENT_CSS_CLASS}__${suffix}`

class UserActions extends Component {
  static propTypes = {
    logoutRequest: PropTypes.func.isRequired,
    user: PropTypes.object
  };

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
    const { history, logoutRequest } = this.props
    if (key === 'logout') {
      logoutRequest()
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
    const { location, user } = this.props
    const { menuOpen } = this.state
    const isActive = (location.pathname === ROUTES.CHANGE_PASSWORD)

    return (
      <div className={COMPONENT_CSS_CLASS}>
        <button
          className={cn(bemE('menu-toggle'), {
            [bemE('menu-toggle--open')]: menuOpen,
            [bemE('menu-toggle--active')]: isActive
          })}
          onClick={this.toggleMenuOpen}
        >
          <span className={bemE('username')}>
            {user.operator.name} ({user.operator.username})
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
          <Menu.Item key="logout">Log off</Menu.Item>
        </Menu>
      </div>
    )
  }
}

const selector = createStructuredSelector({
  user: userSelectors.currentUserSelector,
})

const actions = {
  ...userActions
}

export default compose(
  userIsAuthenticated,
  withRouter,
  connect(selector, actions)
)(UserActions)
