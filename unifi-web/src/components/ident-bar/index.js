import React, { Component } from 'react'

import './index.scss'
import logo from '../../assets/images/unifi-logo.png'

export default class IdentBar extends Component {
  render() {
    return (
      <div className="ident-bar">
        <img className="logo" src={logo} alt="logo" />
      </div>
    )
  }
}
