import React, { Component } from 'react'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'

import { NetworkReaderDetailsCard } from '../../../components'

import './index.scss'

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

const mockData = [
  {
    name: 'Reader 1',
    antennaes: [antennae1, antennae2, antennae3, antennae4],
  },
  {
    name: 'Reader 2',
    antennaes: [antennae1, antennae2, antennae3, antennae4],
  },
  {
    name: 'Reader 3',
    antennaes: [antennae1, antennae2, antennae3, antennae4],
  },
]

class NetworkDiscovery extends Component {
  handleCardDelete = () => {
    // console.log('handleCardDelete', arguments)
  }

  renderNetworkReaderCard = card => (
    <NetworkReaderDetailsCard {...card} />
  )

  render() {
    return (
      <div className="network-discovery">
        {mockData.map(this.renderNetworkReaderCard)}
      </div>
    )
  }
}

export const mapStateToProps = state => state
export const mapDispatch = dispatch => (bindActionCreators({}, dispatch))

export default connect(mapStateToProps, mapDispatch)(NetworkDiscovery)
