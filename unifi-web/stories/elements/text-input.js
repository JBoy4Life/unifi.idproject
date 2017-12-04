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

import { Aux, TextInput } from '../../src/elements'

storiesOf('Elements/TextInput', module)
  .add('simple', () => (
    <Aux>
      <p>Without parent</p>
      <TextInput />
      <p>Restricted by partent width</p>
      <div style={{ maxWidth: 460 }}>
        <TextInput />
      </div>
    </Aux>
  ))
