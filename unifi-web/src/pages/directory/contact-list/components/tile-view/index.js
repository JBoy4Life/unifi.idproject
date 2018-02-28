import React, { Component } from 'react'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link, withRouter } from 'react-router-dom'

import * as ROUTES from 'utils/routes'
import { HolderGrid } from 'components'

const GridItem = HolderGrid.Item

const TileView = ({ holders, viewMode }) => (
  <HolderGrid viewMode={viewMode}>
    {holders.map(item => (
      <GridItem image={item.image} key={item.clientReference}>
        <GridItem.Field>{item.name}</GridItem.Field>
        <GridItem.Field>ID {item.clientReference}</GridItem.Field>
      </GridItem>
    ))}
  </HolderGrid>
)

export default TileView
