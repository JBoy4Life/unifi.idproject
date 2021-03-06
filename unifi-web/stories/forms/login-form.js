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
import { LoginForm } from '../../src/smart-components'

storiesOf('Forms/Login Form', module)
  .add('default', () => (
    <div style={{ maxWidth: 460, padding: 24 }}>
      <LoginForm onSubmit={action('onSubmit')} />
    </div>
  ))
