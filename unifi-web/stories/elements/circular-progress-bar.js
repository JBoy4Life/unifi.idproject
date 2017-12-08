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

import { Aux, CircularProgressBar } from '../../src/elements'

storiesOf('Elements/CircularProgressBar', module)
  .add('simple', () => (
    <Aux>
      <div style={{ width: 300 }}><CircularProgressBar percentage={30} /></div>
      <div style={{ width: 300 }}><CircularProgressBar strokeWidth={4} percentage={30} /></div>
      <div style={{ width: 300 }}><CircularProgressBar percentage={100} /></div>
      <div style={{ width: 300 }}><CircularProgressBar percentage={0} /></div>
    </Aux>
  ))
  .add('evacuation example', () => (
    // 115,200,147
    <div style={{ width: 300, position: 'relative' }}>
      <div style={{ zIndex: 2, position: 'relative' }} >
        <CircularProgressBar strokeWidth={6} percentage={30} />
      </div>
      <div style={{
        position: 'absolute',
        top: '5px',
        bottom: '5px',
        left: '5px',
        right: '5px',
        backgroundColor: '#000',
        borderRadius: '50%',
        zIndex: 1,
      }}
      />
    </div>
  ))
