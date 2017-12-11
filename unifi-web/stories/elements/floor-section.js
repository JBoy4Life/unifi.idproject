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

import { Aux, FloorSection } from '../../src/elements'

storiesOf('Elements/FloorSection', module)
  .add('Floor Status', () => (
    <Aux>
      <div style={{
      width: 300,
      height: 100,
      display: 'flex',
    }}
      >
        <FloorSection label="Section Basic" />
      </div>

      <div style={{
      width: 300,
      height: 100,
      display: 'flex',
    }}
      >
        <FloorSection status="good" label="Section Good" />
      </div>

      <div style={{
      width: 300,
      height: 100,
      display: 'flex',
    }}
      >
        <FloorSection status="warning" label="Section Warning" />
      </div>

      <div style={{
      width: 300,
      height: 100,
      display: 'flex',
    }}
      >
        <FloorSection status="critical" label="Section Critical" />
      </div>
    </Aux>
  ))
  .add('Floor Stairs', () => (
    <Aux>
      <div style={{
      width: 300,
      height: 100,
      display: 'flex',
    }}
      >
        <FloorSection label="Left stairs" stairsPosition="top-left" />
      </div>

      <div style={{
          width: 300,
          height: 100,
          display: 'flex',
        }}
      >
        <FloorSection label="Right stairs" stairsPosition="bottom-right" />
      </div>
      <div style={{
          width: 300,
          height: 100,
          display: 'flex',
        }}
      >
        <FloorSection status="good" label="Colored stairs" stairsPosition="top-right" />
      </div>
    </Aux>
  ))
