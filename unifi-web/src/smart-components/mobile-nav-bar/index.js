import React, { Component } from 'react'
import cn from 'classnames'
import { Icon } from 'elements'
import { compose } from 'redux'

import { LinkedNavigationMenu, UserActions } from '../'
import { userIsAuthenticated } from 'hocs/auth'

import './index.scss'

const COMPONENT_CSS_CLASS = 'mobile__nav-bar'
const bemE = (suffix) => `${COMPONENT_CSS_CLASS}__${suffix}`

class MobileNavbar extends Component {
  constructor(props) {
    super(props);
    this.state = {
      mobileOpen: false
    }
  }

  handleMenuMobile = () => this.setState(prevState => ({ mobileOpen: !prevState.mobileOpen }))

  render() {
    return(
      <div className={COMPONENT_CSS_CLASS}>
        <span onClick={this.handleMenuMobile} className={bemE('button-mobile')}>
          <Icon type="menu-fold" />
        </span>
        {
          this.state.mobileOpen && (
            <div className={bemE('mobile')}>
              <LinkedNavigationMenu />
              <UserActions />
            </div>
          )
        }
      </div>
    )
  }
}


export default compose(
  userIsAuthenticated,
)(MobileNavbar)
