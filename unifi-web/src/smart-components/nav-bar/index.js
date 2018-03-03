import React, { Component } from 'react'
import cn from 'classnames'

import logo from 'assets/images/unifi-logo.svg'
import { Container } from 'elements'
import { LinkedNavigationMenu, UserActions } from '../'

import './index.scss'

const COMPONENT_CSS_CLASS = 'nav-bar'
const bemE = (suffix) => `${COMPONENT_CSS_CLASS}__${suffix}`

const NavBar = () => (
  <div className={COMPONENT_CSS_CLASS}>
    <Container className={bemE('container')}>
      <img className={bemE('logo')} src={logo} alt="logo" />
      <LinkedNavigationMenu />
      <UserActions />
    </Container>
  </div>
)

export default NavBar
