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
      <FlippedCardsStack>
        <EqualSpaceFloorLayout>
          <FloorSection status="good" label="SA1" stairsPosition="top-right" />
          <FloorSection status="warning" label="SA2" />
          <FloorSection status="critical" label="SA3" />
        </EqualSpaceFloorLayout>
        <EqualSpaceFloorLayout>
          <FloorSection status="good" label="SA1" stairsPosition="top-right" />
          <FloorSection status="critical" label="SA3" />
        </EqualSpaceFloorLayout>
        <EqualSpaceFloorLayout>
          <FloorSection status="good" label="SA1XXXSX" stairsPosition="top-right" />
          <FloorSection status="critical" label="SA3" />
        </EqualSpaceFloorLayout>
        <EqualSpaceFloorLayout>
          <FloorSection status="good" label="SA1" stairsPosition="bottom-left" />
        </EqualSpaceFloorLayout>
        <EqualSpaceFloorLayout>
          <FloorSection status="good" label="SA1" stairsPosition="bottm-left" />
          <FloorSection status="critical" label="SA2" />
          <FloorSection status="critical" label="SA3" />
        </EqualSpaceFloorLayout>
        <EqualSpaceFloorLayout>
          <FloorSection status="warning" label="SA1" />
          <FloorSection status="critical" label="SA3" />
        </EqualSpaceFloorLayout>
      </FlippedCardsStack>
    </div>
  ))
  .add('EqualSpaceFloorLayout', () => (
    <div style={{ width: 340, height: 240, display: 'flex' }}>
      <EqualSpaceFloorLayout>
        <FloorSection status="good" label="SA1" stairsPosition="top-right" />
        <FloorSection status="good" label="SA2" />
        <FloorSection status="critical" label="SA3" />
      </EqualSpaceFloorLayout>
    </div>
  ))
