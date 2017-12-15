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
import { NetworkReaderDetailsCard } from '../../src/components'

const antennae1 = {
  id: '1',
  antennaeName: 'ANT465113',
  assignedZone: 'Zone S3B',
  ports: '1',
  status: 'ok',
}

const antennae2 = {
  id: '2',
  antennaeName: 'ANT465113',
  assignedZone: 'Zone S3B',
  ports: '2',
  status: 'danger',
}

const antennae3 = {
  id: '3',
  antennaeName: 'ANT465113',
  ports: '3',
  status: 'danger',
}

const antennae4 = {
  id: '4',
  ports: '4',
}

storiesOf('Components/NetworkReaderDetailsCard', module)
  .add('default', () => (
    <NetworkReaderDetailsCard
      name="Reader name"
      antennaes={[antennae1, antennae2, antennae3, antennae4]}
      onDelete={action('delete reader')}
    />
  ))
