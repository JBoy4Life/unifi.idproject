import React, { Component } from 'react'
import moment from 'moment'

import * as ROUTES from 'utils/routes'
import { AssetGrid } from 'components'

const GridItem = AssetGrid.Item

const formatTime = (value) => moment.utc(value).format('LTS')

const TileView = ({ items, viewMode }) => (
  <AssetGrid viewMode={viewMode}>
    {items.map(item => (
      <GridItem image={item.client.image} key={item.clientReference}>
        <GridItem.Field>{item.client.name}</GridItem.Field>
        <GridItem.Field>ID {item.clientReference}</GridItem.Field>
        <GridItem.Field>{formatTime(item.detectionTime)}</GridItem.Field>
      </GridItem>
    ))}
  </AssetGrid>
)

export default TileView
