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

import { SiteCreationWizzard } from '../../src/smart-components'

const fakeSuccessfulServerSubmit = () => Promise.resolve()

storiesOf('Smart/SiteCreationWizzard', module)
  .add('Happy flow', () => (
    <SiteCreationWizzard
      onStepChange={action('step change')}
      onDetailsSubmit={fakeSuccessfulServerSubmit}
      onZonesSubmit={fakeSuccessfulServerSubmit}
      onDocumentsSubmit={fakeSuccessfulServerSubmit}
      onWizzardDone={action('wizzard done')}
    />
  ))
