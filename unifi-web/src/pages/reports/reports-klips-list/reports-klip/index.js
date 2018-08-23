import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'

export default class ReportsKlip extends Component {
  static propTypes = {
    klip: PropTypes.object.isRequired,
  }

  componentDidMount() {

    const script = document.createElement("script");

    const s = document.createElement('script');
    s.type = 'text/javascript';
    s.async = true;
    s.innerHTML = 'KF.embed.embedKlip({' +
       'profile : "' + this.props.klip.id + '",' +
       'settings : {"width":997,"theme":"light","borderStyle":"round","borderColor":"#cccccc"},' +
       'title : "' + this.props.klip.title + '"' +
    '});'

    document.body.appendChild(s);
  }

  render() {
    return (
      <Fragment>
        <div
          style={{"display":"inline-block"}}
          id={`kf-embed-container-${this.props.klip.id}`}></div>
      </Fragment>
    );
  }
}
