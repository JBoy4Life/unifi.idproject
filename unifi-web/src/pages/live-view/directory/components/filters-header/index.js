import React from 'react'
import { TextInput, Checkbox } from '../../../../../elements'

import './index.scss'

const FiltersHeader = props => (
  <div className="directory-filters-header">
    <TextInput.Search
      className="search-bar"
      placeholder="Search"
      onSearch={props.onSearch}
      enterButton
    />
    <Checkbox onChange={props.onChange}>Contact</Checkbox>
    <Checkbox onChange={props.onChange}>Assets</Checkbox>
    <Checkbox onChange={props.onChange}>Visitors</Checkbox>
  </div>
)

export default FiltersHeader
