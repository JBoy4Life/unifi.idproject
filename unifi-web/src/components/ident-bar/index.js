import React, { Component } from 'react'

import './index.scss'
import logo from '../../assets/images/unifi-logo.png'
import { Button } from '../../elements/index'

export default class IdentBar extends Component {
  render() {
    return (
      <div className="ident-bar">
        <img className="logo" src={logo} alt="logo" />
        {this.props.user && (
          <div className="indent-bar-user-content">
            <span className="indent-bar-user-content-label">{this.props.user}</span>
            <Button onClick={this.props.onLogout}>Logout</Button>
          </div>
        )}
      </div>
    )
  }
}
