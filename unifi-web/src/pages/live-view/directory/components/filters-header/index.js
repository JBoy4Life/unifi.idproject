import React, { Component } from 'react'
import { TextInput, Checkbox } from '../../../../../elements'

import './index.scss'

export default class FiltersHeader extends Component {
  state = {
    searchValue: this.props.searchValue,
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.searchValue !== nextProps.searchValue) {
      this.setState({
        searchValue: nextProps.searchValue,
      })
    }
  }

  onContactFilter = (ev) => {
    this.props.onChange({ contacts: ev.target.checked })
  }

  onAssetFilter = (ev) => {
    this.props.onChange({ assets: ev.target.checked })
  }

  onVisitorsFilter = (ev) => {
    this.props.onChange({ visitors: ev.target.checked })
  }

  handleSearchChange = (ev) => {
    this.setState({ searchValue: ev.target.value })
  }

  render() {
    const { onSearch, filterValues } = this.props

    return (
      <div className="directory-filters-header">
        <TextInput.Search
          className="search-bar"
          placeholder="Search"
          onSearch={onSearch}
          enterButton
          onChange={this.handleSearchChange}
          value={this.state.searchValue}
        />

        <Checkbox
          checked={filterValues.indexOf('contacts') !== -1}
          onChange={this.onContactFilter}
        >
          Contacts
        </Checkbox>

        <Checkbox
          checked={filterValues.indexOf('assets') !== -1}
          onChange={this.onAssetFilter}
        >
          Assets
        </Checkbox>

        <Checkbox
          checked={filterValues.indexOf('visitors') !== -1}
          onChange={this.onVisitorsFilter}
        >
          Visitors
        </Checkbox>

      </div>
    )
  }
}
