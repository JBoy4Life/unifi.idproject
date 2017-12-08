/*
  eslint-disable
  jsx-a11y/accessible-emoji,
  import/no-extraneous-dependencies,
  import/first,
  no-unused-vars
*/
import React from 'react'

import { storiesOf } from '@storybook/react'
import { action } from '@storybook/addon-actions'
import { linkTo } from '@storybook/addon-links'

import { Aux } from '../../src/elements'
import { SideNavigation } from '../../src/components'

import './grid.scss'

const menus = [{
  key: '/evacuation',
  label: 'Dashboard',
},
{
  key: '/evacuation/directory',
  label: 'Directory View',
},
{
  key: '/evacuation/floor',
  label: 'Floor View',
}]

const menusWithIcons = [{
  key: '/evacuation',
  label: 'Dashboard',
  icon: 'piechart',
},
{
  key: '/evacuation/directory',
  label: 'Directory View',
  icon: 'solution',
},
{
  key: '/evacuation/floor',
  label: 'Floor View',
  icon: 'database',
}]

storiesOf('Components/Side navigation', module)
  .add('default', () => (
    <SideNavigation menus={menus} selectedKeys={['/evacuation/floor']} />
  ))
  .add('with icons', () => (
    <SideNavigation menus={menusWithIcons} selectedKeys={['/evacuation/directory']} />
  ))
