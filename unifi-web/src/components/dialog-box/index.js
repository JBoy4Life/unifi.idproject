import React, {Component} from 'react'

import './index.scss'

export default class DialogBox extends Component {
  render() {
    return (
      <div className={`unifi-dialog-box ${this.props.visible ? "visible" : ""}`}>
        <div className="lightbox">
          {this.props.children}
        </div>
      </div>
    );
  }
}
