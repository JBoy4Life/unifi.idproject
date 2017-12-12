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

import { Aux, Steps, Icon } from '../../src/elements'

const { Step } = Steps

storiesOf('Elements/Steps', module)
  .add('Simple Steps', () => (
    <Steps current={1}>
      <Step title="Finished" description="This is a description." />
      <Step title="In Progress" description="This is a description." />
      <Step title="Waiting" description="This is a description." />
    </Steps>
  ))
  .add('Loading Steps', () => (
    <Steps>
      <Step status="finish" title="Login" icon={<Icon type="user" />} />
      <Step status="finish" title="Verification" icon={<Icon type="solution" />} />
      <Step status="process" title="Pay" icon={<Icon type="loading2" />} />
      <Step status="wait" title="Done" icon={<Icon type="smile-o" />} />
    </Steps>
  ))
