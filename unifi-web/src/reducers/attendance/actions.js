import { WSPackage } from '../../lib/ws'

import { LIST_SCHEDULE_STATS } from './types'

export function listScheduleStats() {

  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion:  '1.0.0',
    messageType:     'attendance.schedule.list-schedule-stats',
    payload:         { clientId: "ucl-mgmt" }
  });

  return {
    type: LIST_SCHEDULE_STATS,
    socketRequest: pack.content
  };

}

export default null
