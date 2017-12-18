import React, { Component } from 'react'

import { Icon, Button, Card, Table } from '../../elements'

import './index.scss'

const getIconType = (statusText) => {
  switch (statusText) {
    case 'danger':
      return 'exclamationcircle'
    case 'ok':
      return 'checkcircle'
    default:
      return null
  }
}

const getIcon = (statusText) => {
  const iconType = getIconType(statusText)
  if (iconType) {
    return <Icon type={iconType} />
  }
  return null
}

const columns = [{
  title: 'Port',
  dataIndex: 'ports',
  key: 'ports',
}, {
  title: 'Antennae',
  dataIndex: 'antennaeName',
  key: 'antennaeName',
}, {
  title: 'Assigned',
  dataIndex: 'assignedZone',
  key: 'assignedZone',
}, {
  title: '',
  dataIndex: 'status',
  key: 'status',
  render: getIcon,
}]

class NetworkReaderDetailsCard extends Component {
  state = {
    inEditMode: false,
  }

  handleEditButtonClicked = () => {
    this.setState({ inEditMode: true })
  }

  handleCancelEditButtonClick = () => {
    this.setState({ inEditMode: false })
  }

  renderHeader() {
    return (
      <div>
        {this.props.name}
        {
          this.state.inEditMode
          ? '' : <Icon onClick={this.handleEditButtonClicked} type="edit" />
        }
      </div>
    )
  }

  renderDisplayContent() {
    const { antennaes } = this.props
    return (
      <Table
        className="network-reader-antennaes-list"
        dataSource={antennaes}
        columns={columns}
        pagination={false}
      />
    )
  }

  renderEditContent() {
    return (
      <div className="network-reader-confirm-delete-container">
        <Button onClick={this.props.onDelete} type="danger">Delete</Button>
        <Button onClick={this.handleCancelEditButtonClick}>Cancel</Button>
      </div>
    )
  }

  render() {
    return (
      <Card className="network-reader-details-card" title={this.renderHeader()}>
        {this.state.inEditMode && this.renderEditContent()}
        {this.renderDisplayContent()}
      </Card>
    )
  }
}

export default NetworkReaderDetailsCard
