import React, {Component} from 'react'

import './index.scss'

export default class SearchableSelectField extends Component {
  constructor(props) {
    super(props);
    this.state = {
      searchTerm: "",
      selectedKey: null
    };
  }
  itemDeselect() {
    this.setState({
      selectedKey: null
    });
    document.getElementById(this.props.inputId).focus();
    this.props.onSelectionClear();
  }
  itemSelect(key) {
    this.setState({
      searchTerm: "",
      selectedKey: key
    });
    this.props.onItemSelect(key);
  }
  searchTermChange(newTerm) {
    this.setState({
      searchTerm: newTerm
    });
  }
  render() {
    return (
      <div className={`unifi-searchable-select-field ${this.props.containerClassName || ""}`}>
        <input id={this.props.inputId}
               className={this.props.inputClassName || ""}
               value={this.state.searchTerm}
               onChange={(event) => this.searchTermChange(event.target.value)} />
        <ul>
          {Object.keys(this.props.data)
            .filter((k) => {
              const haystack = this.props.data[k].trim().toLowerCase();
              const needle   = this.state.searchTerm.trim().toLowerCase();
              return needle.length > 0 && haystack.indexOf(needle) > -1;
            })
            .map((k) => {
              return <li key={k}
                         onClick={() => this.itemSelect(k)}>{this.props.data[k]}</li>
            })}
        </ul>
        <p className={`${this.state.selectedKey ? "visible" : ""}`}
           onClick={() => this.itemDeselect()}>{this.state.selectedKey}</p>
      </div>

    );
  }
}
