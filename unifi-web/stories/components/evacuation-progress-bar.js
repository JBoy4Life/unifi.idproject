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
import { EvacuationProgressBar } from '../../src/components'

storiesOf('Components/EvacuationProgressBar', module)
  .add('default', () => (
    <Aux>
      <div style={{ maxWidth: 150 }}>
        <EvacuationProgressBar percentage={0} />
      </div>
      <div style={{ maxWidth: 150 }}>
        <EvacuationProgressBar percentage={30} />
      </div>
      <div style={{ maxWidth: 150 }}>
        <EvacuationProgressBar percentage={75} />
      </div>
      <div style={{ maxWidth: 150 }}>
        <EvacuationProgressBar percentage={100} />
      </div>
    </Aux>
  ))
