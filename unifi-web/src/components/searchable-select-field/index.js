import React, {Component} from 'react'
import cn from 'classnames'

import './index.scss'

export default class SearchableSelectField extends Component {
  constructor(props) {
    super(props);
    this.state = {
      searchTerm: "",
      selectedKey: null,
      resultsVisible: false
    };
  }

  handleItemDeselect = () => {
    this.setState({
      selectedKey: null
    });
    document.getElementById(this.props.inputId).focus();
    this.props.onSelectionClear();
  }

  handleItemSelect = (key) => () => {
    this.setState({
      searchTerm: "",
      selectedKey: key,
      resultsVisible: false
    });
    this.props.onItemSelect(key)
  }

  handleInputFocus = () => {
    this.setState({
      resultsVisible: true
    })
  }

  handleSearchTermChange = (event) => {
    this.setState({
      searchTerm: event.target.value
    })
  }

  render() {
    const { data, inputId, inputClassName, containerClassName } = this.props
    const { searchTerm, resultsVisible, selectedKey } = this.state

    return (
      <div className={cn('unifi-searchable-select-field', containerClassName)}>
        <input
          id={inputId}
          className={inputClassName || ""}
          value={searchTerm}
          onChange={this.handleSearchTermChange}
          onFocus={this.handleInputFocus}
        />
        <ul>
          {Object.keys(data)
            .filter((k) => {
              const haystack = data[k].trim().toLowerCase();
              const needle   = searchTerm.trim().toLowerCase();
              return resultsVisible && haystack.indexOf(needle) > -1;
            })
            .map((k) => (
              <li key={k} onClick={this.handleItemSelect(k)}>{data[k]}</li>
            ))
          }
        </ul>
        <p 
          className={`${selectedKey ? "visible" : ""}`}
          onClick={this.handleItemDeselect}
        >
          {data[selectedKey]}
        </p>
      </div>

    )
  }
}
