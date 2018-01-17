import React, { Component } from 'react'

import logo from 'assets/images/ucl-logo.svg'
import { Button } from 'elements'

import './index.scss'

const COMPONENT_CSS_CLASS = 'ident-bar'
const bemE = (suffix) => `${COMPONENT_CSS_CLASS}__${suffix}`

export default class IdentBar extends Component {
  render() {
    return (
      <div className={COMPONENT_CSS_CLASS}>
        <img className={bemE('logo')} src={logo} alt="logo" />
        {this.props.user && (
          <div className={bemE('user-content')}>
            {/*<span className={bemE('user-content-label')}>{this.props.user}</span>*/}
            <Button onClick={this.props.onLogout}>Logout</Button>
          </div>
        )}
      </div>
    )
  }
}
