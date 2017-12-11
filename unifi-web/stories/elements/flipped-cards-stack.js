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

import { Aux, FloorSection, FlippedCardsStack, EqualSpaceFloorLayout } from '../../src/elements'

storiesOf('Elements/FloorView', module)
  .add('Flipped Cards Layout', () => (
    <div style={{
      width: 500,
      margin: 'auto',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
    }}
    >
      <FlippedCardsStack />
    </div>
  ))
  .add('EqualSpaceFloorLayout', () => (
    <EqualSpaceFloorLayout>
      <div>SA1</div>
      <div>SA2</div>
      <div>SA3</div>
    </EqualSpaceFloorLayout>
  ))
