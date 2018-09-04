import React, { Component } from 'react'
import moment from 'moment-timezone'

import * as ROUTES from 'config/routes'
import { HolderGrid } from 'components'

const GridItem = HolderGrid.Item

const formatTime = (value, timeZone) => moment(value).tz(timeZone).format('LTS')

const TileView = ({ items, viewMode, timeZone }) => (
  <HolderGrid viewMode={viewMode}>
    {items.map(item => (
      <GridItem image={item.client.image} key={item.clientReference}>
        <GridItem.Field>{item.client.name}</GridItem.Field>
        <GridItem.Field>ID {item.clientReference}</GridItem.Field>
        <GridItem.Field>{formatTime(item.detectionTime, timeZone)}</GridItem.Field>
      </GridItem>
    ))}
  </HolderGrid>
)

export default TileView
