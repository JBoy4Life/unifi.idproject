import React, { Component } from 'react'
import { List } from 'antd';

import ReportsKlip from './reports-klip'

//
// GOT THIS IDS AND TITLES FROM KLIPFOLIO DASHBOARD GOING TO A KLIP AND CLICKING SHARE EMBEBED KLIPS
//
const klips = [
  {
    id: "50203518da421deb48296ac33760b129",
    title: "Drop-In Member Hours",
  },
  {
    id: "dc3d6490a595733bd47824a843bcb921",
    title: "Drop In Members Visits",
  },
  {
    id: "f422f62777d804503204d95ecfaf2a65",
    title: "Average Hours Per Site, Per Day, for Drop In Members",
  },
  {
    id: "ac78524539a379e221c34d2183491ed0",
    title: "July Drop In Visits By Site",
  },
];

export default class ReportsKlipsList extends Component {
  render() {
    return (
      <div className="reports__klips-container">
        <List
          dataSource={this.props.klips}
          renderItem={klip => (<List.Item><ReportsKlip klip={klip} /></List.Item>)} />
      </div>
    );
  }
}
